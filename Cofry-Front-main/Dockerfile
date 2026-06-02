# Stage 1: Build the Angular SSR application
FROM node:20-alpine AS build
WORKDIR /app

# Copy package descriptors
COPY package*.json ./

# Install dependencies (including devDependencies for building)
RUN npm install

# Copy rest of the application files
COPY . .

# Build the project
RUN npm run build

# Stage 2: Run the application
FROM node:20-alpine
WORKDIR /app

# Copy built app and package info
COPY --from=build /app/dist ./dist
COPY --from=build /app/package*.json ./

# Install only production dependencies
RUN npm install --omit=dev

# Expose the default SSR port
EXPOSE 4000

# Set environment variables for host and port binding
ENV PORT=4000
ENV HOST=0.0.0.0

# Command to run the SSR server
CMD ["node", "dist/Cofry-FrontEnd/server/server.mjs"]
