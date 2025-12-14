#!/usr/bin/env python3
"""
Watchdog Service - Surveillance et redémarrage automatique du core_backend
Le watchdog vérifie la disponibilité du service toutes les 30 secondes.
Si le service ne répond pas, il se connecte en SSH et redémarre le container Docker.
"""

import requests
import subprocess
import time
import logging
import json
import sys
import os
from datetime import datetime
from pathlib import Path
from typing import Dict, Tuple
from config import WatchdogConfig, ServiceConfig

# Créer le répertoire de logs s'il n'existe pas
log_dir = Path('/var/log/watchdog')
log_dir.mkdir(parents=True, exist_ok=True)

# Configuration du logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(log_dir / 'watchdog.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)


class CoreBackendWatchdog:
    """
    Classe pour surveiller plusieurs instances de core_backend et les redémarrer si nécessaire
    """

    def __init__(self, config: WatchdogConfig):
        self.config = config
        # Dictionnaire pour tracker l'état de chaque service
        self.service_states = {
            service.name: {'failure_count': 0, 'service': service}
            for service in config.services
        }

    def check_service_health(self, service: ServiceConfig) -> Tuple[bool, str]:
        """
        Vérifie la santé d'une instance de service via l'endpoint /api/watchdog/health
        
        Args:
            service: Configuration du service à vérifier
        
        Returns:
            Tuple[bool, str]: (service_is_healthy, message)
        """
        health_url = f"http://{service.host}:{service.port}/api/watchdog/health"
        
        try:
            response = requests.get(
                health_url,
                timeout=self.config.health_check_timeout
            )
            
            if response.status_code == 200:
                data = response.json()
                if data.get("status") == "UP":
                    logger.info(f"✓ [{service.name}] Service healthy - Response: {data}")
                    return True, "Service is UP"
                else:
                    logger.warning(f" [{service.name}] Service returned unexpected status: {data}")
                    return False, f"Unexpected status: {data.get('status')}"
            else:
                logger.warning(f" [{service.name}] Service returned HTTP {response.status_code}")
                return False, f"HTTP {response.status_code}"
                
        except requests.exceptions.Timeout:
            logger.error(f"✗ [{service.name}] Health check timeout")
            return False, "Timeout"
        except requests.exceptions.ConnectionError as e:
            logger.error(f"✗ [{service.name}] Connection error: {e}")
            return False, f"Connection error: {str(e)}"
        except Exception as e:
            logger.error(f"✗ [{service.name}] Unexpected error: {e}")
            return False, f"Error: {str(e)}"

    def restart_service_via_ssh(self, service: ServiceConfig) -> bool:
        """
        Se connecte au serveur via SSH et redémarre le container Docker du service
        
        Args:
            service: Configuration du service à redémarrer
        
        Returns:
            bool: True si le redémarrage a réussi, False sinon
        """
        logger.info(f" [{service.name}] Attempting to restart via SSH...")
        logger.info(f"   Connexion: {service.ssh_user}@{service.ssh_host}:{service.ssh_port}")

        try:
            # Construire la commande SSH
            ssh_command = [
                "ssh",
                "-p", str(service.ssh_port),
                f"{service.ssh_user}@{service.ssh_host}",
                f"cd {service.docker_compose_path} && echo {service.ssh_mdp} | sudo -S docker-compose up -d {service.docker_service_name}"
            ]

            logger.debug(f"SSH Command: {' '.join(ssh_command)}")

            # Exécuter la commande SSH
            result = subprocess.run(
                ssh_command,
                timeout=self.config.ssh_timeout,
                capture_output=True,
                text=True
            )

            if result.returncode == 0:
                logger.info(f"✓ [{service.name}] Docker service restarted successfully!")
                logger.info(f"Output: {result.stdout}")
                return True
            else:
                logger.error(f"✗ [{service.name}] SSH command failed with code {result.returncode}")
                logger.error(f"Error: {result.stderr}")
                return False

        except subprocess.TimeoutExpired:
            logger.error(f"✗ [{service.name}] SSH command timed out (timeout={self.config.ssh_timeout}s)")
            return False
        except FileNotFoundError:
            logger.error(f"✗ [{service.name}] SSH command not found - ensure OpenSSH is installed and in PATH")
            return False
        except Exception as e:
            logger.error(f"✗ [{service.name}] Failed to restart via SSH: {e}")
            return False

    def restart_service_with_docker_api(self, service: ServiceConfig) -> bool:
        """
        Alternative: redémarre le service en utilisant l'API Docker locale
        (utile si SSH n'est pas disponible)
        
        Args:
            service: Configuration du service à redémarrer
        
        Returns:
            bool: True si le redémarrage a réussi, False sinon
        """
        try:
            logger.info(f" [{service.name}] Attempting to restart docker container: {service.docker_container_name}")
            
            docker_command = [
                "docker",
                "restart",
                service.docker_container_name
            ]

            result = subprocess.run(
                docker_command,
                timeout=30,
                capture_output=True,
                text=True
            )

            if result.returncode == 0:
                logger.info(f"✓ [{service.name}] Docker container restarted successfully!")
                return True
            else:
                logger.error(f"✗ [{service.name}] Docker restart failed: {result.stderr}")
                return False

        except Exception as e:
            logger.error(f"✗ [{service.name}] Failed to restart via Docker API: {e}")
            return False

    def run(self):
        """
        Boucle principale du watchdog
        Vérifie la santé de tous les services toutes les X secondes
        """
        logger.info(f" Watchdog started - Monitoring interval: {self.config.check_interval}s")
        logger.info(f"   Monitoring {len(self.service_states)} service(s)")
        for service_name, state in self.service_states.items():
            service = state['service']
            logger.info(f"   - {service.name}: {service.host}:{service.port}")
        logger.info("-" * 80)

        try:
            while True:
                # Vérifier la santé de tous les services
                for service_name, state in self.service_states.items():
                    service = state['service']
                    failure_count = state['failure_count']
                    
                    # Vérifier la santé du service
                    is_healthy, message = self.check_service_health(service)

                    if is_healthy:
                        # Service est healthy, réinitialiser le compteur
                        state['failure_count'] = 0
                    else:
                        # Service n'est pas sain
                        failure_count += 1
                        state['failure_count'] = failure_count
                        logger.warning(f" [{service.name}] Failure count: {failure_count}/{self.config.max_consecutive_failures}")
                        
                        # Si trop d'échecs, redémarrer le service
                        if failure_count >= self.config.max_consecutive_failures:
                            logger.critical(f" [{service.name}] Service unreachable - Initiating restart procedure!")
                            
                            # Essayer d'abord le redémarrage via SSH
                            restart_success = self.restart_service_via_ssh(service)
                            
                            # Si SSH échoue, essayer via Docker API locale
                            if not restart_success and service.use_docker_api_fallback:
                                logger.info(f"[{service.name}] Falling back to Docker API restart...")
                                restart_success = self.restart_service_with_docker_api(service)

                            if restart_success:
                                # Attendre un peu avant de re-vérifier
                                logger.info(f"[{service.name}] ⏳ Waiting {self.config.recovery_wait_time}s before health check...")
                                time.sleep(self.config.recovery_wait_time)
                                state['failure_count'] = 0
                            else:
                                logger.critical(f" [{service.name}] Failed to restart service! Manual intervention required!")

                # Attendre avant la prochaine vérification
                time.sleep(self.config.check_interval)

        except KeyboardInterrupt:
            logger.info("Watchdog stopped by user")
            sys.exit(0)
        except Exception as e:
            logger.critical(f"Fatal error in watchdog: {e}")
            sys.exit(1)


def main():
    """Fonction principale"""
    try:
        # Charger la configuration
        config = WatchdogConfig.from_file('config.json')
        logger.info(f"Configuration loaded from config.json")
        
        # Créer et lancer le watchdog
        watchdog = CoreBackendWatchdog(config)
        watchdog.run()
        
    except FileNotFoundError:
        logger.error("config.json not found. Using default configuration.")
        config = WatchdogConfig()
        watchdog = CoreBackendWatchdog(config)
        watchdog.run()
    except Exception as e:
        logger.error(f"Error: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
