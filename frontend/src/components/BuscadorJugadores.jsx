import React, { useState, useEffect } from 'react';
import api from '../services/api';
import './css/BuscadorJugadores.css';

function BuscadorJugadores({ usuario }) {
    const [jugadores, setJugadores] = useState([]);
    const [busqueda, setBusqueda] = useState('');
    const [cargando, setCargando] = useState(true);

    // Estado clave para saber qué jugador se ha clicado
    const [jugadorSeleccionado, setJugadorSeleccionado] = useState(null);
    const [ultimosPartidos, setUltimosPartidos] = useState([]);

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

    // Al hacer clic en un jugador, cargamos sus detalles y sus partidos
    const handleSeleccionarJugador = async (jugador) => {
        setJugadorSeleccionado(jugador);
        setCargandoPartidos(true);

        try {
            // LLAMADA REAL: Conecta con el @GetMapping("/recent") de tu MatchController
            const response = await api.get(`/matches/recent?team=${encodeURIComponent(jugador.team)}`);
            setUltimosPartidos(response.data); // Guarda la lista de objetos { texto: "..." }
        } catch (error) {
            console.error("Error al consultar partidos en tiempo real. Usando respaldo:", error);
            // Tu plan de rescate visual si el backend falla
            setUltimosPartidos([
                { id: 1, texto: "Sin partidos recientes (Offline)" }
            ]);
        } finally {
            setCargandoPartidos(false);
        }
    };
    // Coincidencia de caracteres dinámica (Filtra de inmediato a cada letra introducida)
    const jugadoresFiltrados = jugadores.filter(jugador =>
        jugador.name.toLowerCase().includes(busqueda.toLowerCase()) ||
        jugador.team.toLowerCase().includes(busqueda.toLowerCase())
    );

    // Mapeo amigable de la posición para cumplir con tu nota de diseño
    const mapearPosicion = (pos) => {
        if (pos === 'ATTACKER') return 'Delantero';
        if (pos === 'MIDFIELDER') return 'Centrocampista';
        if (pos === 'DEFENDER') return 'Defensa';
        if (pos === 'GOALKEEPER') return 'Portero';
        return pos;
    };

    // Si hay un jugador seleccionado, mostramos la ficha técnica (Fiel a tu imagen)
    if (jugadorSeleccionado) {
        return (
            <div className="buscador-container perfil-jugador-box">
                <div className="perfil-header">
                    <div className="perfil-identificacion">
                        <div className="avatar-jugador-xl">🏃‍♂️</div>
                        <div>
                            <h2>{jugadorSeleccionado.name}</h2>
                            <p style={{ color: '#64748b', margin: 0 }}>{jugadorSeleccionado.team}</p>
                        </div>
                    </div>

                    {!esInvitado && (
                        <button className="btn-fav-perfil" onClick={() => alert('Añadido a favoritos')}>
                            ⭐ Añadir a Favoritos
                        </button>
                    )}
                </div>

                <div className="grid-info-datos">
                    {/* Bloque Izquierdo: Información Básica y Estadísticas Generales */}
                    <div className="bloque-datos">
                        <h4>Información básica y estadísticas</h4>
                        <ul>
                            <li>🏢 <strong>Club:</strong> {jugadorSeleccionado.team}</li>
                            <li>🎯 <strong>Posición:</strong> {mapearPosicion(jugadorSeleccionado.position)}</li>
                            <li>⚽ <strong>Goles totales:</strong> {jugadorSeleccionado.totalGoals}</li>
                            <li>👟 <strong>Asistencias totales:</strong> {jugadorSeleccionado.totalAssists}</li>
                            <li>🏃‍♂️ <strong>Partidos jugados:</strong> {jugadorSeleccionado.partidosJugados || 18}</li>
                            <li>🛡️ <strong>Partidos como titular:</strong> {jugadorSeleccionado.partidosTitular || 15}</li>
                        </ul>
                    </div>

                    {/* Bloque Derecho: Historial Reciente (Tu PowerPoint) */}
                    <div className="bloque-datos">
                        <h4>Últimos 5 partidos de su club</h4>
                        <div>
                            {ultimosPartidos.map((partido, index) => (
                                <div key={partido.id || index} className="partido-item">
                                    P{index + 1}: {partido.texto}
                                </div>
                            ))}
                        </div>
                    </div>
                </div>

                <button onClick={() => setJugadorSeleccionado(null)} className="btn-volver">
                    ← Volver al buscador
                </button>
            </div>
        );
    }

    // Vista principal: Buscador + Tabla
    return (
        <div className="buscador-container">
            <h2>🔍 Buscador Avanzado de Jugadores</h2>
            <p>Introduce las iniciales para filtrar en tiempo real y haz clic en cualquier fila para ver el análisis extendido.</p>

            <div className="search-bar-container">
                <input
                    type="text"
                    placeholder="Escribe para buscar (ej. Vin)..."
                    className="search-input"
                    value={busqueda}
                    onChange={(e) => setBusqueda(e.target.value)}
                />
            </div>

            {cargando ? (
                <p>Procesando base de datos deportiva...</p>
            ) : (
                <table className="tabla-jugadores">
                    <thead>
                        <tr>
                            <th>Nombre</th>
                            <th>Posición</th>
                            <th>Club</th>
                            <th>Goles ⚽</th>
                            <th>Asistencias 👟</th>
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
        </div>
    );
}

export default BuscadorJugadores;