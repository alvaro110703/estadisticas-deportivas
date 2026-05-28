import React, { useState, useEffect } from 'react';
import api from '../services/api';
import './css/BuscadorJugadores.css';

function BuscadorJugadores({ usuario, favoritos = [], onVolverAlInicio, onUpdateFavoritos, jugadorInicial }) {
    console.log("Datos del usuario en el Buscador:", usuario);
    const [jugadores, setJugadores] = useState([]);
    const [busqueda, setBusqueda] = useState('');
    const [cargando, setCargando] = useState(true);

    const [jugadorSeleccionado, setJugadorSeleccionado] = useState(null);
    const [ultimosPartidos, setUltimosPartidos] = useState([]);
    const [cargandoPartidos, setCargandoPartidos] = useState([]);
    const esInvitado = usuario?.rol === 'GUEST';

    // Cargar la lista completa de jugadores al arrancar
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

    // 🌟 NUEVO EFFECT: Detecta si entramos al componente con un jugador pre-seleccionado desde Favoritos 🌟
    useEffect(() => {
        if (jugadorInicial) {
            // Reutilizamos el manejador para cargar sus partidos en tiempo real e inicializar la vista de perfil
            handleSeleccionarJugador(jugadorInicial);
        }
    }, [jugadorInicial]);

    const handleSeleccionarJugador = async (jugador) => {
        setCargandoPartidos(true);
        const nombreClub = jugador.team || jugador.club;

        let partidosDelClub = [];
        let totalPartidosTemporada = undefined;

        try {
            // Lanzamos ambas peticiones HTTP al mismo tiempo de manera asíncrona
            const [responseRecientes, responseTotal] = await Promise.all([
                api.get(`/matches/recent?team=${encodeURIComponent(nombreClub)}`),
                api.get(`/matches/total-count?team=${encodeURIComponent(nombreClub)}`)
            ]);

            partidosDelClub = responseRecientes.data;
            totalPartidosTemporada = responseTotal.data; // Recibe el número entero directo del backend

            setUltimosPartidos(Array.isArray(partidosDelClub) ? partidosDelClub : []);
        } catch (error) {
            console.error("Error al consultar datos deportivos en tiempo real:", error);
            setUltimosPartidos([]);
        } finally {
            setCargandoPartidos(false);
        }

        // 🧮 CÁLCULO AL VUELO BASADO EN EL CONTADOR TOTAL DEL ENPOINT NUEVO 🧮
        let partidosJugadosCalculados = undefined;
        let partidosTitularCalculados = undefined;

        // Si el backend respondió un número válido y mayor que cero, realizamos las operaciones
        if (totalPartidosTemporada !== undefined && totalPartidosTemporada > 0) {
            partidosJugadosCalculados = Math.ceil(totalPartidosTemporada * 0.9);
            partidosTitularCalculados = Math.ceil(partidosJugadosCalculados * 0.7);
        }

        const jugadorNormalizado = {
            ...jugador,
            id: jugador.id,
            name: jugador.name || jugador.nombre,
            team: nombreClub,
            position: jugador.position || jugador.posicion,
            totalGoals: jugador.totalGoals !== undefined ? jugador.totalGoals : jugador.golesTotales,
            totalAssists: jugador.totalAssists !== undefined ? jugador.totalAssists : jugador.asistenciasTotales,
            partidosJugados: partidosJugadosCalculados,
            partidosTitular: partidosTitularCalculados
        };

        setJugadorSeleccionado(jugadorNormalizado);
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

    const añadirAFavoritos = async () => {
        if (!usuario || !usuario.correo) {
            alert("Error: No se ha detectado el correo del usuario");
            return;
        }

        try {
            const response = await fetch(`http://localhost:8080/api/favorites/add-by-email/${usuario.correo}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    id: Number(jugadorSeleccionado.id),
                    name: jugadorSeleccionado.name,
                    team: jugadorSeleccionado.team,
                    position: jugadorSeleccionado.position
                })
            });

            if (response.ok) {
                alert('¡Jugador añadido a favoritos correctamente!');
                
                // 1. 🌟 Forzamos que el jugador actual se inserte visualmente de inmediato en la lista local de props
                favoritos.push({
                    id: jugadorSeleccionado.id,
                    name: jugadorSeleccionado.name,
                    team: jugadorSeleccionado.team,
                    position: jugadorSeleccionado.position
                });

                // 2. 🚀 Sincronizamos el Dashboard para actualizar las barras laterales en segundo plano
                if (onUpdateFavoritos) onUpdateFavoritos();
                
                // 3. Forzamos el renderizado del perfil para que recalcule el botón
                setJugadorSeleccionado({ ...jugadorSeleccionado });
            } else {
                const errorText = await response.text();
                alert(`No se pudo añadir: ${errorText}`);
            }
        } catch (error) {
            console.error("Error al conectar con el servidor:", error);
        }
    };

    const quitarDeFavoritos = async () => {
        if (!usuario || !usuario.correo) return;

        try {
            const response = await fetch(`http://localhost:8080/api/favorites/remove-by-email/${usuario.correo}/${jugadorSeleccionado.id}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (response.ok) {
                alert('¡Jugador eliminado de favoritos correctamente!');
                
                // 1. 🌟 Filtramos localmente y vaciamos al jugador del array de inmediato sin esperar a la API
                const indice = favoritos.findIndex(f => String(f.id) === String(jugadorSeleccionado.id));
                if (indice !== -1) {
                    favoritos.splice(indice, 1);
                }

                // 2. 🚀 Sincronizamos el Dashboard para vaciar las barras laterales
                if (onUpdateFavoritos) onUpdateFavoritos();

                // 3. Forzamos el renderizado del perfil para que recalcule el botón
                setJugadorSeleccionado({ ...jugadorSeleccionado });
            } else {
                const errorText = await response.text();
                alert(`No se pudo eliminar: ${errorText}`);
            }
        } catch (error) {
            console.error("Error al conectar con el servidor:", error);
        }
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
                        favoritos.some(f => String(f.id) === String(jugadorSeleccionado.id || jugadorSeleccionado.apiMatchId)) ? (
                            <button
                                className="btn-fav-perfil"
                                onClick={quitarDeFavoritos}
                                style={{ backgroundColor: '#ef4444', color: 'white' }}
                            >
                                ❌ Quitar de Favoritos
                            </button>
                        ) : (
                            <button
                                className="btn-fav-perfil"
                                onClick={añadirAFavoritos}
                            >
                                ⭐ Añadir a Favoritos
                            </button>
                        )
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
                            <li><strong>Partidos jugados:</strong> {jugadorSeleccionado.partidosJugados}</li>
                            <li><strong>Partidos como titular:</strong> {jugadorSeleccionado.partidosTitular}</li>
                        </ul>
                    </div>

                    <div className="bloque-datos">
                        <h4>Últimos 5 partidos de su club</h4>
                        <div className="partidos-recientes-lista">
                            {cargandoPartidos ? (
                                <p style={{ fontSize: '13px', color: '#64748b' }}>Consultando encuentros recientes...</p>
                            ) : (
                                ultimosPartidos.map((partido, index) => {
                                    const partidoTexto = partido.texto || "";
                                    const match = partidoTexto.match(/^(.+?)\s+(\d+)\s*-\s*(\d+)\s+(.+)$/);

                                    let letra = 'E';
                                    let claseColor = 'resultado-e';

                                    if (match) {
                                        const equipoLocal = match[1].trim();
                                        const golesLocal = parseInt(match[2], 10);
                                        const golesVisitante = parseInt(match[3], 10);
                                        const clubDelJugador = jugadorSeleccionado?.team || "";

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
                                })
                            )}
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