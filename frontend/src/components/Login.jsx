import React, { useState } from 'react';
import api from '../services/api';
import './css/AuthForm.css';

function Login({ onBack, onLoginSuccess }) {
    // 1. Modificamos el estado inicial para usar 'identificador' en lugar de 'correo'
    const [credentials, setCredentials] = useState({ identificador: '', contrasena: '' });
    const [error, setError] = useState(null);
    const [cargando, setCargando] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setCredentials({ ...credentials, [name]: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setCargando(true);

        try {
            // Envía el objeto con { identificador, contrasena } tal y como espera el DTO
            const response = await api.post('/users/login', credentials);

            // Si el login es exitoso, mapeamos la respuesta real que devuelve tu backend
            onLoginSuccess({
                nombre: response.data.nombre,
                correo: response.data.correo,
                rol: response.data.rol || 'USER'
            });
        } catch (err) {
            setError(err.response?.data || "Error al iniciar sesión. Comprueba tus datos.");
        } finally {
            setCargando(false);
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <h2 className="auth-title">🔐 Iniciar Sesión</h2>
                <p className="auth-subtitle">Introduce tus credenciales para acceder</p>

                {error && <div className="auth-error-box">❌ {error}</div>}

                <form onSubmit={handleSubmit} className="auth-form">
                    {/* 2. Cambiamos el input para permitir texto libre (Nombre o Correo) */}
                    <div className="auth-input-group">
                        <label className="auth-label">Usuario o Correo Electrónico</label>
                        <input
                            type="text"
                            name="identificador"
                            value={credentials.identificador}
                            onChange={handleChange}
                            className="auth-input"
                            required
                            disabled={cargando}
                        />
                    </div>
                    <div className="auth-input-group">
                        <label className="auth-label">Contraseña</label>
                        <input
                            type="password"
                            name="contrasena"
                            value={credentials.contrasena}
                            onChange={handleChange}
                            className="auth-input"
                            required
                            disabled={cargando}
                        />
                    </div>
                    <button type="submit" className="auth-btn-submit btn-login-color" disabled={cargando}>
                        {cargando ? 'Accediendo...' : 'Ingresar'}
                    </button>
                </form>
               <div style={{ display: 'flex', justifyContent: 'center', marginTop: '15px' }}>
                    <button onClick={onBack} className="btn-nav-icono" title="Volver al menú de inicio">
                        🏠
                    </button>
                </div>
            </div>
        </div>
    );
}

export default Login;