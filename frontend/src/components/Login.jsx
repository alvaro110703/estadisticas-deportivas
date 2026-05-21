import React, { useState } from 'react';
import api from '../services/api';
import './css/AuthForm.css'; 

function Login({ onBack, onLoginSuccess }) {
    const [credentials, setCredentials] = useState({ correo: '', contrasena: '' });
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
            const response = await api.post('/users/login', credentials);
            onLoginSuccess({
                nombre: response.data.nombre || credentials.correo,
                correo: credentials.correo,
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
                    <div className="auth-input-group">
                        <label className="auth-label">Correo Electrónico</label>
                        <input type="email" name="correo" value={credentials.correo} onChange={handleChange} className="auth-input" required disabled={cargando} />
                    </div>
                    <div className="auth-input-group">
                        <label className="auth-label">Contraseña</label>
                        <input type="password" name="contrasena" value={credentials.contrasena} onChange={handleChange} className="auth-input" required disabled={cargando} />
                    </div>
                    <button type="submit" className="auth-btn-submit btn-login-color" disabled={cargando}>
                        {cargando ? 'Accediendo...' : 'Ingresar'}
                    </button>
                </form>
                <button onClick={onBack} className="auth-btn-back" disabled={cargando}>← Volver al Menú</button>
            </div>
        </div>
    );
}

export default Login;