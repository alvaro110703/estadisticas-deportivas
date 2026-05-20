import React, { useEffect, useState } from 'react';
import api from './services/api'; // Importamos tu configuración de Axios

function App() {
  const [datos, setDatos] = useState(null);
  const [error, setError] = useState(null);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    // 📝 CAMBIA ESTA RUTA por un endpoint tuyo que sepas que funciona (ej: '/positions', '/players', etc.)
    api.get('/players/search?name=vini') 
      .then(response => {
        setDatos(response.data);
        setCargando(false);
      })
      .catch(err => {
        console.error("Error en la conexión:", err);
        setError(err.message);
        setCargando(false);
      });
  }, []);

  return (
    <div style={{ padding: '30px', fontFamily: 'sans-serif', maxWidth: '800px', margin: '0 auto' }}>
      <h1>🧪 Prueba de Conexión Frontend-Backend</h1>
      <hr />

      {cargando && <p style={{ color: 'orange' }}>⏳ Conectando con Spring Boot...</p>}
      
      {error && (
        <div style={{ padding: '15px', backgroundColor: '#ffeedd', color: '#cc3300', borderRadius: '5px' }}>
          <h3>❌ Error de Conexión</h3>
          <p>{error}</p>
          <small>Revisa que el Backend esté corriendo en el puerto 8080 y tengas el CORS configurado.</small>
        </div>
      )}

      {datos && (
        <div style={{ padding: '15px', backgroundColor: '#eemedd', color: '#006633', borderRadius: '5px' }}>
          <h3>✅ ¡Conexión Exitosa!</h3>
          <p>Tu Spring Boot ha respondido correctamente. Aquí tienes los datos crudos recibidos:</p>
          <pre style={{ backgroundColor: '#222', color: '#fff', padding: '15px', borderRadius: '5px', overflowX: 'auto' }}>
            {JSON.stringify(datos, null, 2)}
          </pre>
        </div>
      )}
    </div>
  );
}

export default App;