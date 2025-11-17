# Multi-stage Dockerfile pour une application React créée avec create-react-app
# Étape de build
FROM node:18-alpine AS build
WORKDIR /app

# Copier package.json et package-lock.json si présent, puis installer
COPY package.json package-lock.json* ./
RUN npm ci --silent

# Copier le reste et builder
COPY . .
RUN npm run build

# Étape de production : servir avec nginx
FROM nginx:stable-alpine
COPY --from=build /app/build /usr/share/nginx/html

# Optional: custom nginx config can be added here
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
