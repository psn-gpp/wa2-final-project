// https://vite.dev/config/
import {defineConfig} from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
    plugins: [react()],
    base: '/ui/',       // Ensure correct path resolution
    build: {
        emptyOutDir: true,
        outDir: '../ReservationService/src/main/resources/ui',  //Direct build output
    },
});