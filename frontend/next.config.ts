import type { NextConfig } from "next";

const isProduction = process.env.NODE_ENV === "production";

const productionConfig: NextConfig = { output: "export", distDir: "build", }

const devConfig: NextConfig = { // Mimic the behavior of a reverse proxy in development
    async rewrites() {
        return [
            {
                source: '/api/:path*',
                destination: 'http://localhost:8080/api/:path*', // Your Spring Boot port
            },
            {
                source: '/ws/:path*',
                destination: 'http://localhost:8080/ws/:path*',
            },
        ];
    },
}

const nextConfig: NextConfig = isProduction ? productionConfig : devConfig;

export default nextConfig