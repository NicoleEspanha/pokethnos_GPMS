# build
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# a Vite "embute" essa URL no bundle em tempo de build — precisa ser o
# endereço que o NAVEGADOR do jogador vai usar para falar com o backend
# (não o nome do serviço docker, que só existe dentro da rede do compose).
ARG VITE_API_BASE_URL=http://localhost:8080
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build

# runtime
FROM nginx:1.27-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
