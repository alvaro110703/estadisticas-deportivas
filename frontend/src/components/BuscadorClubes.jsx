import React, { useState, useEffect } from 'react';
import api from '../services/api';
import './css/BuscadorClubes.css';

function BuscadorClubes({ usuario, onVolverAlInicio }) {
    const [clubes, setClubes] = useState([]);
    const [jugadores, setJugadores] = useState([]);
    const [busqueda, setBusqueda] = useState('');
    const [cargando, setCargando] = useState(true);

    // Estados de navegación interna
    const [clubSeleccionado, setClubSeleccionado] = useState(null);
    const [ultimosPartidos, setUltimosPartidos] = useState([]);
    const [maxGoleador, setMaxGoleador] = useState(null);

    useEffect(() => {
        const cargarDatos = async () => {
            try {
                const [resClubes, resJugadores] = await Promise.all([
                    api.get('/teams'),
                    api.get('/players')
                ]);
                setClubes(Array.isArray(resClubes.data) ? resClubes.data : []);
                setJugadores(Array.isArray(resJugadores.data) ? resJugadores.data : []);
            } catch (error) {
                console.error("Error cargando datos deportivos:", error);
            } finally {
                setCargando(false);
            }
        };
        cargarDatos();
    }, []);

    const obtenerInfoLiga = (code) => {
    switch (code?.toUpperCase()) {
        case 'PD':
            return { nombre: 'LaLiga EA Sports', pais: 'España 🇪🇸' };
        case 'PL':
            return { nombre: 'Premier League', pais: 'Inglaterra 🇬🇧' };
        case 'BL1':
            return { nombre: 'Bundesliga', pais: 'Alemania 🇩🇪' };
        case 'FL1':
            return { nombre: 'Ligue 1', pais: 'Francia 🇫🇷' };
        case 'SA':
            return { nombre: 'Serie A', pais: 'Italia 🇮🇹' };
        case 'NO_ASIGNADO':
        default:
            return { nombre: 'Liga Desconocida', pais: 'Sin Asignar 🌐' };
    }
};

    const handleSeleccionarClub = async (club) => {
        setClubSeleccionado(club);
        
        // Encontrar el Máximo Goleador REAL del club recorriendo tus jugadores en BD
        const clubNameNorm = String(club.name || '').toLowerCase();
        const jugadoresDelClub = jugadores.filter(j => String(j.team || '').toLowerCase() === clubNameNorm);
        
        if (jugadoresDelClub.length > 0) {
            const topScorer = jugadoresDelClub.reduce((prev, current) => 
                ((prev.totalGoals || 0) > (current.totalGoals || 0)) ? prev : current
            );
            setMaxGoleador(topScorer);
        } else {
            setMaxGoleador(null);
        }

        // Consultar los últimos partidos en tiempo real de este club
        try {
            const response = await api.get(`/matches/recent?team=${encodeURIComponent(club.name)}`);
            setUltimosPartidos(Array.isArray(response.data) ? response.data : []); 
        } catch (error) {
            console.error("Error consultando partidos del club:", error);
            setUltimosPartidos([]);
        }
    };

    const clubesFiltrados = clubes.filter(club => {
        if (!club || !club.name) return false;
        const info = obtenerInfoLiga(club.competitionCode);
        const term = busqueda.toLowerCase();
        return club.name.toLowerCase().includes(term) ||
               info.nombre.toLowerCase().includes(term) ||
               info.pais.toLowerCase().includes(term);
    });

    if (clubSeleccionado) {
        const infoLiga = obtenerInfoLiga(clubSeleccionado.competitionCode);
        
        return (
            <div className="buscador-container perfil-club-box">
                <div className="perfil-header">
                    <div className="perfil-identificacion">
                        <h2>🛡️ {clubSeleccionado.name}</h2>
                        <p style={{ color: '#64748b', margin: 0, fontSize: '14px' }}>
                            {infoLiga.nombre} ({infoLiga.pais})
                        </p>
                    </div>
                </div>

                <div className="grid-info-datos">
                    <div className="bloque-datos">
                        <h4>Estadísticas de Temporada</h4>
                        <ul className="lista-info-basica">
                            <li><strong>Partidos Ganados:</strong> {clubSeleccionado.victorias}</li>
                            <li><strong>Partidos Empatados:</strong> {clubSeleccionado.empates}</li>
                            <li><strong>Partidos Perdidos:</strong> {clubSeleccionado.derrotas}</li>
                            <hr style={{ border: '0', borderTop: '1px solid #e2e8f0', margin: '8px 0' }} />
                            <li><strong>Goles a Favor:</strong> ⚽ {clubSeleccionado.golesFavor}</li>
                            <li><strong>Goles en Contra:</strong> 🛡️ {clubSeleccionado.golesContra}</li>
                            <hr style={{ border: '0', borderTop: '1px solid #e2e8f0', margin: '8px 0' }} />
                            <li>
                                <strong>Máximo Goleador:</strong> {maxGoleador ? `${maxGoleador.name} (${maxGoleador.totalGoals} goles)` : 'No registrado'}
                            </li>
                        </ul>
                    </div>

                    <div className="bloque-datos">
                        <h4>Últimos 5 resultados</h4>
                        <div className="partidos-recientes-lista">
                            {ultimosPartidos.length > 0 ? (
                                ultimosPartidos.map((partido, index) => {
                                    const partidoTexto = partido.texto || "";
                                    const match = partidoTexto.match(/^(.+?)\s+(\d+)\s*-\s*(\d+)\s+(.+)$/);

                                    let letra = 'E';
                                    let claseColor = 'resultado-e';

                                    if (match) {
                                        const equipoLocal = match[1].trim();
                                        const golesLocal = parseInt(match[2], 10);
                                        const golesVisitante = parseInt(match[3], 10);
                                        const clubActual = clubSeleccionado.name;

                                        if (golesLocal === golesVisitante) {
                                            letra = 'E';
                                            claseColor = 'resultado-e';
                                        } else {
                                            const esLocal = equipoLocal.toLowerCase().includes(clubActual.toLowerCase());
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
                            ) : (
                                <p style={{ fontSize: '13px', color: '#64748b' }}>No hay partidos registrados para este club.</p>
                            )}
                        </div>
                    </div>
                </div>

                <div className="botonera-navegacion-inferior">
                    <button onClick={() => setClubSeleccionado(null)} className="btn-nav-icono" title="Volver al buscador">
                        ↩️
                    </button>
                    <button onClick={onVolverAlInicio} className="btn-nav-icono" title="Volver al menú de inicio">
                        🏠
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="buscador-container">
            <div className="search-bar-container">
                <input
                    type="text"
                    className="search-input"
                    placeholder="Buscar club, país o competición..."
                    value={busqueda}
                    onChange={(e) => setBusqueda(e.target.value)}
                />
            </div>

            {cargando ? (
                <p className="estado-mensaje">Accediendo a los registros de las ligas...</p>
            ) : busqueda.trim() === '' ? (
                <div className="teclea-prompt">
                    <p>💡 Teclea para mostrar coincidencias...</p>
                </div>
            ) : (
                <table className="tabla-clubes">
                    <thead>
                        <tr>
                            <th>Nombre del Club</th>
                            <th>País</th>
                            <th>Competición</th>
                        </tr>
                    </thead>
                    <tbody>
                        {clubesFiltrados.length > 0 ? (
                            clubesFiltrados.map(club => {
                                const infoLiga = obtenerInfoLiga(club.competitionCode);
                                return (
                                    <tr
                                        key={club.id}
                                        className="fila-clicable"
                                        onClick={() => handleSeleccionarClub(club)}
                                    >
                                        <td><strong>{club.name}</strong></td>
                                        <td>{infoLiga.pais}</td>
                                        <td>{infoLiga.nombre}</td>
                                    </tr>
                                );
                            })
                        ) : (
                            <tr>
                                <td colSpan="3" className="no-results">
                                    No se han encontrado clubes para "{busqueda}"
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            )}

            <div className="botonera-navegacion-inferior">
                <button onClick={onVolverAlInicio} className="btn-nav-icono" title="Volver al menú de inicio">
                    🏠
                </button>
            </div>
        </div>
    );
}

export default BuscadorClubes;