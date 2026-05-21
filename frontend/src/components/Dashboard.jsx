import React, { useState } from 'react';
import './css/Dashboard.css';
import BuscadorJugadores from './BuscadorJugadores';

function Dashboard({ usuario, onLogout }) {
    const [seccion, setSeccion] = useState('INICIO');
    const [menuAbierto, setMenuAbierto] = useState(false);

    const esInvitado = usuario?.rol === 'GUEST';

    return (
        <div className="dashboard-container">

            {/* ================= BARRA SUPERIOR (HEADER) ================= */}
            <header className="header">
                <div className="logo" onClick={() => setSeccion('INICIO')}>
                    ⚽ <span>ESTADÍSTICAS DEPORTIVAS</span>
                </div>

                <div className="user-container">
                    <div className="avatar-circle" onClick={() => setMenuAbierto(!menuAbierto)}>
                        {usuario?.fotoUrl ? (
                            <img src={usuario.fotoUrl} alt="Perfil" className="avatar-img" />
                        ) : (
                            usuario?.nombre?.charAt(0).toUpperCase()
                        )}
                    </div>

                    {menuAbierto && (
                        <div className="dropdown-menu">
                            <div className="dropdown-header">
                                <strong>{usuario?.nombre}</strong>
                                <span className="role-text">{usuario?.rol}</span>
                            </div>
                            <button onClick={() => { setSeccion('PERFIL'); setMenuAbierto(false); }} className="dropdown-item">
                                ⚙️ Editar información de usuario
                            </button>
                            <button onClick={onLogout} className="dropdown-item dropdown-item-logout">
                                🚪 Cerrar sesión
                            </button>
                        </div>
                    )}
                </div>
            </header>

            {/* ================= CUERPO PRINCIPAL ================= */}
            <div className="body-layout">

                <main className="main-content">
                    {seccion === 'INICIO' && (
                        <div>
                            <div className="grid-opciones">

                                {/* 1. REALIZAR COMPARACIONES */}
                                <div
                                    className={`card-opcion ${esInvitado ? 'card-deshabilitada' : ''}`}
                                    onClick={() => !esInvitado && setSeccion('COMPARACIONES')}
                                >
                                    <div className="icon-big">📊</div>
                                    <h3>REALIZAR COMPARACIONES</h3>
                                    {esInvitado && <div className="badge-candado">🔒 Bloqueado para Invitados</div>}
                                </div>

                                {/* 2. BUSCADOR JUGADORES */}
                                <div className="card-opcion" onClick={() => setSeccion('BUSCADOR_JUG')}>
                                    <div className="icon-big">🏃‍♂️</div>
                                    <h3>BUSCADOR JUGADORES</h3>
                                </div>

                                {/* 3. BUSCADOR CLUBES */}
                                <div className="card-opcion" onClick={() => setSeccion('BUSCADOR_CLUB')}>
                                    <div className="icon-big">🛡️</div>
                                    <h3>BUSCADOR CLUBES</h3>
                                </div>

                            </div>
                        </div>
                    )}

                    {/* SECCIONES DINÁMICAS */}
                    {seccion === 'COMPARACIONES' && (
                        <div>
                            <h2>📊 Módulo de Comparación de Jugadores</h2>
                            <button onClick={() => setSeccion('INICIO')} className="btn-volver">← Volver al inicio</button>
                        </div>
                    )}

                    {seccion === 'BUSCADOR_JUG' && (
                        <div>
                            <h2>🔍 Buscador de Jugadores</h2>
                            <BuscadorJugadores
                                usuario={usuario}
                                onVolverAlInicio={() => setSeccion('INICIO')}
                            />
                        </div>
                    )}

                    {seccion === 'BUSCADOR_CLUB' && (
                        <div>
                            <h2>🛡️ Buscador de Clubes</h2>
                            <button onClick={() => setSeccion('INICIO')} className="btn-volver">← Volver al inicio</button>
                        </div>
                    )}

                    {seccion === 'PERFIL' && (
                        <div className="profile-container">
                            <h2>⚙️ Configuración del Perfil</h2>
                            <p>Modifica tus datos de usuario:</p>
                            <div className="upload-box">
                                <div className="avatar-circle" style={{ width: '100px', height: '100px', fontSize: '32px' }}>
                                    {usuario?.nombre?.charAt(0).toUpperCase()}
                                </div>
                                <button className="btn-upload">Añadir nueva foto de perfil</button>
                            </div>
                            <button onClick={() => setSeccion('INICIO')} className="btn-volver">← Volver al inicio</button>
                        </div>
                    )}

                    {seccion === 'FAVORITOS_ALL' && (
                        <div>
                            <h2>⭐ Lista Completa de Favoritos</h2>
                            <button onClick={() => setSeccion('INICIO')} className="btn-volver">← Volver al inicio</button>
                        </div>
                    )}
                </main>

                {/* ================= SECCIÓN DE FAVORITOS (DERECHA) ================= */}
                <aside className="sidebar-favoritos">
                    <h3 className="fav-title">⭐ FAVORITOS (máx 5)</h3>

                    {esInvitado ? (
                        <div className="fav-candado-box">
                            <span style={{ fontSize: '24px' }}>🔒</span>
                            <p style={{ fontSize: '13px', margin: '5px 0 0 0' }}>Bloqueado</p>
                        </div>
                    ) : (
                        <div className="fav-list">

                            <span onClick={() => setSeccion('FAVORITOS_ALL')} className="ver-mas-link">Ver más...</span>
                        </div>
                    )}
                </aside>

            </div>
        </div>
    );
}

export default Dashboard;