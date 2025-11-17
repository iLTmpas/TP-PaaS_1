package fr.episen.core.backend.access.config;

import fr.episen.core.backend.access.handler.BadgeScanHandler;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * Classe de configuration pour l'intégration MQTT avec Spring Integration.
 * Configure la connexion au broker MQTT et l'abonnement aux topics pour le scan de badges.
 */
@Slf4j
@Configuration
public class MqttConfig {

    // Injection des propriétés de configuration MQTT depuis application.properties/yml
    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    @Value("${mqtt.client-id}")
    private String clientId;

    @Value("${mqtt.topic-scan}")
    private String topicScan;

    /**
     * Crée et configure la factory pour les clients MQTT.
     *
     * @return MqttPahoClientFactory configurée avec les options de connexion
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        // Configuration de l'URL du broker MQTT
        options.setServerURIs(new String[]{brokerUrl});
        // Nettoie la session à chaque connexion (pas de messages persistants)
        options.setCleanSession(true);
        // Reconnexion automatique en cas de perte de connexion
        options.setAutomaticReconnect(true);
        // Timeout de connexion en secondes
        options.setConnectionTimeout(30);
        // Intervalle de keep-alive pour maintenir la connexion active
        options.setKeepAliveInterval(60);

        factory.setConnectionOptions(options);
        return factory;
    }

    /**
     * Crée le canal de messagerie pour recevoir les messages MQTT entrants.
     *
     * @return MessageChannel de type DirectChannel pour un traitement synchrone
     */
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    /**
     * Configure l'adaptateur pour recevoir les messages MQTT de manière asynchrone.
     * S'abonne au topic configuré et redirige les messages vers le canal d'entrée.
     *
     * @return MqttPahoMessageDrivenChannelAdapter configuré
     */
    @Bean
    public MqttPahoMessageDrivenChannelAdapter inbound() {
        // Création de l'adaptateur avec un ID client unique pour la subscription
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        clientId + "-sub",
                        mqttClientFactory(),
                        topicScan
                );

        // Timeout pour la complétion des opérations en millisecondes
        adapter.setCompletionTimeout(5000);
        // Convertisseur par défaut pour transformer les messages MQTT en messages Spring
        adapter.setConverter(new DefaultPahoMessageConverter());
        // Qualité de service MQTT : 1 = au moins une fois (at least once)
        adapter.setQos(1);
        // Définit le canal de sortie où les messages seront envoyés
        adapter.setOutputChannel(mqttInputChannel());

        log.info("✅ MQTT Subscribe configuré sur: {}", topicScan);
        return adapter;
    }

    /**
     * Crée le gestionnaire de messages pour traiter les messages MQTT reçus.
     * Délègue le traitement au BadgeScanHandler avec gestion d'erreur.
     *
     * @param badgeScanHandler Le handler métier pour traiter les scans de badges
     * @return MessageHandler configuré comme service activator
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler(BadgeScanHandler badgeScanHandler) {
        return message -> {
            try {
                // Délégation du traitement au handler métier
                badgeScanHandler.handleMessage(message);
            } catch (Exception e) {
                // Logging des erreurs sans interrompre le flux de messages
                log.error("❌ Erreur traitement message MQTT", e);
            }
        };
    }
}