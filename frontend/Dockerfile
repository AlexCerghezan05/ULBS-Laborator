# D-040: build cu Vite, servit de nginx.
FROM node:26.13.1-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-fund --no-audit
COPY . .
RUN npm run build

FROM nginx:1.31.9-alpine
# D-044: configurarea e un șablon; la pornire imaginea pune variabilele de mediu și scrie
# /etc/nginx/conf.d/default.conf. Implicit backend-ul din Compose; Render îl suprascrie.
ENV P21_BACKEND_URL=http://backend:8080 \
    NGINX_ENTRYPOINT_LOCAL_RESOLVERS=1
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html
