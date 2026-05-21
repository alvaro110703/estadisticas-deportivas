import React, { useState } from 'react';
import api from '../services/api';
import './css/AuthForm.css'; 

function Register({ onBack, onRegisterSuccess }) {
    const [formData, setFormData] = useState({ nombre: '', correo: '', contrasena: '' });
    const [error, setError] = useState(null);
    const [mensajeExito, setMensajeExito] = useState(null);
    const [cargando, setCargando] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setMensajeExito(null);

        if (!formData.nombre || !formData.correo || !formData.contrasena) {
            setError("Por favor, rellena todos los campos.");
            return;
        }

        setCargando(true);
        try {
            const response = await api.post('/users/register', formData);
            setMensajeExito(response.data);
            setTimeout(() => onRegisterSuccess(), 2000);
        } catch (err) {
            setError(err.response?.data || "Error al conectar con el servidor.");
        } finally {
            setCargando(false);
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <h2 className="auth-title">📝 Registro de Usuario</h2>
                <p className="auth-subtitle">Crea una cuenta para guardar tus estadísticas favoritas</p>

                {error && <div className="auth-error-box">❌ {error}</div>}
                {mensajeExito && <div className="auth-success-box">✅ {mensajeExito}</div>}

                <form onSubmit={handleSubmit} className="auth-form">
                    <div className="auth-input-group">
                        <label className="auth-label">Nombre</label>
                        <input type="text" name="nombre" value={formData.nombre} onChange={handleChange} className="auth-input" required disabled={cargando} />
                    </div>
                    <div className="auth-input-group">
                        <label className="auth-label">Correo Electrónico</label>
                        <input type="email" name="correo" value={formData.correo} onChange={handleChange} className="auth-input" required disabled={cargando} />
                    </div>
                    <div className="auth-input-group">
                        <label className="auth-label">Contraseña</label>
                        <input type="password" name="contrasena" value={formData.contrasena} onChange={handleChange} className="auth-input" required disabled={cargando} />
                    </div>
                    <button type="submit" className="auth-btn-submit btn-register-color" disabled={cargando}>
                        {cargando ? 'Registrando...' : 'Registrar Cuenta'}
                    </button>
                </form>
                <button onClick={onBack} className="auth-btn-back" disabled={cargando}>← Volver al Menú</button>
            </div>
        </div>
    );
}

export default Register;