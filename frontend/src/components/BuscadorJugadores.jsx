import React, { useState, useEffect } from 'react';
import api from '../services/api';
import './css/BuscadorJugadores.css';

function BuscadorJugadores({ usuario, onVolverAlInicio }) {
    const [jugadores, setJugadores] = useState([]);
    const [busqueda, setBusqueda] = useState('');
    const [cargando, setCargando] = useState(true);

    const [jugadorSeleccionado, setJugadorSeleccionado] = useState(null);
    const [ultimosPartidos, setUltimosPartidos] = useState([]);
    const [cargandoPartidos, setCargandoPartidos] = useState([]);
    const esInvitado = usuario?.rol === 'GUEST';

    useEffect(() => {
        const obtenerJugadores = async () => {
            try {
                const response = await api.get('/players');
                setJugadores(response.data);
            } catch (error) {
                console.error("Error cargando jugadores:", error);
            } finally {
                setCargando(false);
            }
        };
        obtenerJugadores();
    }, []);

    const handleSeleccionarJugador = async (jugador) => {
        setJugadorSeleccionado(jugador);
        setCargandoPartidos(true);

        try {
            const response = await api.get(`/matches/recent?team=${encodeURIComponent(jugador.team)}`);
            setUltimosPartidos(response.data); 
        } catch (error) {
            console.error("Error al consultar partidos en tiempo real. Usando respaldo:", error);
            setUltimosPartidos([
                { id: 1, texto: "Sin partidos recientes (Offline)" }
            ]);
        } finally {
            setCargandoPartidos(false);
        }
    };

    const jugadoresFiltrados = jugadores.filter(jugador =>
        jugador.name.toLowerCase().includes(busqueda.toLowerCase()) ||
        jugador.team.toLowerCase().includes(busqueda.toLowerCase())
    );

    const mapearPosicion = (pos) => {
        if (pos === 'ATTACKER') return 'Delantero';
        if (pos === 'MIDFIELDER') return 'Centrocampista';
        if (pos === 'DEFENDER') return 'Defensa';
        if (pos === 'GOALKEEPER') return 'Portero';
        return pos;
    };

    // ================= VISTA DE DETALLES DEL JUGADOR =================
    if (jugadorSeleccionado) {
        return (
            <div className="buscador-container perfil-jugador-box">
                <div className="perfil-header">
                    <div className="perfil-identificacion">
                        <div>
                            <h2>{jugadorSeleccionado.name}</h2>
                            <p style={{ color: '#64748b', margin: 0, fontSize: '14px' }}>{jugadorSeleccionado.team}</p>
                        </div>
                    </div>

                    {!esInvitado && (
                        <button className="btn-fav-perfil" onClick={() => alert('Añadido a favoritos')}>
                            ⭐ Añadir a Favoritos
                        </button>
                    )}
                </div>

                <div className="grid-info-datos">
                    <div className="bloque-datos">
                        <h4>Información básica y estadísticas</h4>
                        <ul className="lista-info-basica">
                            <li><strong>Club:</strong> {jugadorSeleccionado.team}</li>
                            <li><strong>Posición:</strong> {mapearPosicion(jugadorSeleccionado.position)}</li>
                            <li><strong>Goles totales:</strong> {jugadorSeleccionado.totalGoals}</li>
                            <li><strong>Asistencias totales:</strong> {jugadorSeleccionado.totalAssists}</li>
                            <li><strong>Partidos jugados:</strong> {jugadorSeleccionado.partidosJugados || 18}</li>
                            <li><strong>Partidos como titular:</strong> {jugadorSeleccionado.partidosTitular || 15}</li>
                        </ul>
                    </div>

                    <div className="bloque-datos">
                        <h4>Últimos 5 partidos de su club</h4>
                        <div className="partidos-recientes-lista">
                            {ultimosPartidos.map((partido, index) => {
                                const partidoTexto = partido.texto || "";
                                const match = partidoTexto.match(/^(.+?)\s+(\d+)\s*-\s*(\d+)\s+(.+)$/);

                                let letra = 'E';
                                let claseColor = 'resultado-e';

                                if (match) {
                                    const equipoLocal = match[1].trim();
                                    const golesLocal = parseInt(match[2], 10);
                                    const golesVisitante = parseInt(match[3], 10);
                                    const clubDelJugador = jugadorSeleccionado?.team || "Rayo Vallecano";

                                    if (golesLocal === golesVisitante) {
                                        letra = 'E';
                                        claseColor = 'resultado-e';
                                    } else {
                                        const esLocal = equipoLocal.toLowerCase().includes(clubDelJugador.toLowerCase());
                                        if (esLocal) {
                                            letra = golesLocal > golesVisitante ? 'V' : 'D';
                                            claseColor = golesLocal > golesVisitante ? 'resultado-v' : 'resultado-d';
                                        } else {
                                            letra = golesVisitante > golesLocal ? 'V' : 'D';
                                            claseColor = golesVisitante > golesLocal ? 'resultado-v' : 'resultado-d';
                                        }
                                    }
                                }

                                return (
                                    <div key={partido.id || index} className="partido-item">
                                        <span className="partido-texto">{partidoTexto}</span>
                                        <div className={`badge-resultado ${claseColor}`}>
                                            {letra}
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                </div>

                {/* BOTONERA INFERIOR EN EL PERFIL: VOLVER ATRÁS Y HOME */}
                <div className="botonera-navegacion-inferior">
                    <button 
                        onClick={() => setJugadorSeleccionado(null)} 
                        className="btn-nav-icono" 
                        title="Volver al buscador"
                    >
                        ↩️
                    </button>
                    <button 
                        onClick={onVolverAlInicio} 
                        className="btn-nav-icono" 
                        title="Volver al menú de inicio"
                    >
                        🏠
                    </button>
                </div>
            </div>
        );
    }

    // ================= VISTA PRINCIPAL (TABLA / BUSCADOR) =================
    return (
        <div className="buscador-container">
            <div className="search-bar-container">
                <input
                    type="text"
                    className="search-input"
                    placeholder="Buscar por jugador o club..."
                    value={busqueda}
                    onChange={(e) => setBusqueda(e.target.value)}
                />
            </div>

            {cargando ? (
                <p className="estado-mensaje">Procesando base de datos deportiva...</p>
            ) : busqueda.trim() === '' ? (
                <div className="teclea-prompt">
                    <p>💡 Teclea para mostrar coincidencias...</p>
                </div>
            ) : (
                <table className="tabla-jugadores">
                    <thead>
                        <tr>
                            <th>Nombre</th>
                            <th>Posición</th>
                            <th>Club</th>
                            <th>Goles</th>
                            <th>Asistencias</th>
                        </tr>
                    </thead>
                    <tbody>
                        {jugadoresFiltrados.length > 0 ? (
                            jugadoresFiltrados.map(jugador => (
                                <tr
                                    key={jugador.id}
                                    className="fila-clicable"
                                    onClick={() => handleSeleccionarJugador(jugador)}
                                >
                                    <td><strong>{jugador.name}</strong></td>
                                    <td>{mapearPosicion(jugador.position)}</td>
                                    <td>{jugador.team}</td>
                                    <td>{jugador.totalGoals}</td>
                                    <td>{jugador.totalAssists}</td>
                                </tr>
                            ))
                        ) : (
                            <tr>
                                <td colSpan="5" className="no-results">
                                    No se han encontrado coincidencias para "{busqueda}"
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            )}

            {/* BOTONERA INFERIOR EN EL BUSCADOR: SÓLO CASITA PARA IR AL MENÚ */}
            <div className="botonera-navegacion-inferior">
                <button 
                    onClick={onVolverAlInicio} 
                    className="btn-nav-icono" 
                    title="Volver al menú de inicio"
                >
                    🏠
                </button>
            </div>
        </div>
    );
}

export default BuscadorJugadores;