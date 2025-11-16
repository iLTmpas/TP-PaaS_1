# Utilise Nginx comme base
FROM nginx:alpine

# Supprime la config par défaut de Nginx
RUN rm -rf /usr/share/nginx/html/*

# Copie le build frontend dans le dossier de Nginx
COPY src/main/resources/ /usr/share/nginx/html/

# Expose le port 80
EXPOSE 80

# Commande par défaut
CMD ["nginx", "-g", "daemon off;"]
