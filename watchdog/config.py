"""
Configuration du Watchdog Service
Permet de personnaliser le comportement du watchdog via config.json
"""

import json
from dataclasses import dataclass, asdict, field
from pathlib import Path
from typing import List, Optional


@dataclass
class ServiceConfig:
    """Configuration pour une instance de service à surveiller"""
    
    name: str  # Nom du service (ex: "core_backend_1")
    host: str  # IP ou hostname
    port: int  # Port HTTP
    ssh_host: str  # IP/hostname du serveur SSH
    ssh_port: int = 22
    ssh_user: str = "root"
    ssh_mdp: str = None  # Mot de passe SSH (optionnel si clé SSH utilisée)
    docker_compose_path: str = "/path/to/core_backend"
    docker_service_name: str = "core_backend"
    docker_container_name: str = "core_backend"
    use_docker_api_fallback: bool = True

@dataclass
class WatchdogConfig:
    """Configuration du watchdog service"""
    
    # === Configuration du monitoring global ===
    check_interval: int = 30  # Intervalle de vérification en secondes
    max_consecutive_failures: int = 2  # Nombre d'échecs avant redémarrage
    recovery_wait_time: int = 15  # Temps d'attente après redémarrage avant re-vérification
    health_check_timeout: int = 10  # Timeout pour les requêtes HTTP (secondes)
    ssh_timeout: int = 30  # Timeout pour les commandes SSH
    
    # === Services à surveiller ===
    services: List[ServiceConfig] = field(default_factory=list)
    
    @classmethod
    def from_file(cls, config_path: str) -> 'WatchdogConfig':
        """
        Charger la configuration depuis un fichier JSON
        
        Args:
            config_path: Chemin du fichier config.json
            
        Returns:
            WatchdogConfig: Instance de configuration
        """
        try:
            with open(config_path, 'r') as f:
                data = json.load(f)
            
            # Convertir la liste des services en objets ServiceConfig
            services_data = data.pop('services', [])
            services = [ServiceConfig(**service) for service in services_data]
            
            # Créer la configuration
            config = cls(**data)
            config.services = services
            return config
        except FileNotFoundError:
            raise FileNotFoundError(f"Configuration file not found: {config_path}")
        except json.JSONDecodeError:
            raise ValueError(f"Invalid JSON in configuration file: {config_path}")
        except TypeError as e:
            raise ValueError(f"Invalid configuration fields: {e}")
    
    def to_file(self, config_path: str) -> None:
        """
        Sauvegarder la configuration dans un fichier JSON
        
        Args:
            config_path: Chemin du fichier config.json
        """
        config_dir = Path(config_path).parent
        config_dir.mkdir(parents=True, exist_ok=True)
        
        # Convertir les objets ServiceConfig en dictionnaires
        config_dict = asdict(self)
        config_dict['services'] = [asdict(service) for service in self.services]
        
        with open(config_path, 'w') as f:
            json.dump(config_dict, f, indent=2)
        
        print(f"Configuration saved to {config_path}")
    
    def __str__(self) -> str:
        """Retourner une représentation lisible de la configuration"""
        lines = ["Watchdog Configuration:", "-" * 80]
        lines.append(f"Check Interval: {self.check_interval}s")
        lines.append(f"Max Consecutive Failures: {self.max_consecutive_failures}")
        lines.append(f"Recovery Wait Time: {self.recovery_wait_time}s")
        lines.append("")
        lines.append("Services to Monitor:")
        for service in self.services:
            lines.append(f"  - {service.name}:")
            lines.append(f"      Host: {service.host}:{service.port}")
            lines.append(f"      SSH: {service.ssh_user}@{service.ssh_host}:{service.ssh_port}")
            lines.append(f"      Docker: {service.docker_container_name} ({service.docker_service_name})")
        return "\n".join(lines)
