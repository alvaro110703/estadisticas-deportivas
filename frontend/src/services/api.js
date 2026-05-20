import axios from 'axios';

// Creamos una instancia personalizada de Axios para tu TFG
const api = axios.create({
  baseURL: 'http://localhost:8080/api', // 👈 Asegúrate de que esta es la ruta base de tus @RestController
  timeout: 10000, // Si el backend tarda más de 10 segundos, corta la petición por seguridad
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
});

export default api;