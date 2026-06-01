import React, { useState, useEffect } from 'react';
import api from '../services/api';
import './css/ModuloComparacion.css'; // Asegúrate de crear el CSS correspondiente

function ModuloComparacion({ usuario, onVolverAlInicio }) {
    // Las 4 ranuras para los jugadores de la comparación
    const [slots, setSlots] = useState([null, null, null, null]);
    const [slotActivo, setSlotActivo] = useState(null);

    // Estados para el sub-buscador de asignación
    const [jugadoresBusqueda, setJugadoresBusqueda] = useState([]);
    const [busqueda, setBusqueda] = useState('');
    const [cargandoBusqueda, setCargandoBusqueda] = useState(false);
    const [mostrarBuscador, setMostrarBuscador] = useState(false);

    // Filtros y Resultados
    const [filtroContexto, setFiltroContexto] = useState('');
    const [resultados, setResultados] = useState(null);
    const [procesandoCalculos, setProcesandoCalculos] = useState(false);

    // Contamos cuántos jugadores hay seleccionados actualmente
    const jugadoresSeleccionadosCount = slots.filter(s => s !== null).length;
    const botonCompararDesbloqueado = jugadoresSeleccionadosCount >= 2 && filtroContexto !== '';

    // Cargar todos los jugadores del sistema una sola vez para el sub-buscador
    useEffect(() => {
        const cargarJugadoresParaFichar = async () => {
            try {
                setCargandoBusqueda(true);
                const response = await api.get('/players');
                setJugadoresBusqueda(response.data);
            } catch (error) {
                console.error("Error obteniendo jugadores para el módulo de comparación:", error);
            } finally {
                setCargandoBusqueda(false);
            }
        };
        cargarJugadoresParaFichar();
    }, []);

    // Manejar la apertura del buscador para un slot específico
    const abrirBuscadorParaSlot = (index) => {
        setSlotActivo(index);
        setBusqueda('');
        setMostrarBuscador(true);
        setResultados(null); // Limpiamos resultados previos si se altera el ecosistema
    };

    // Añadir el jugador al slot activo
    const agregarJugadorASlot = (jugador) => {
        const nuevosSlots = [...slots];
        nuevosSlots[slotActivo] = {
            id: jugador.id,
            name: jugador.name,
            team: jugador.team,
            position: jugador.position,
            totalGoals: jugador.totalGoals || 0,
            totalAssists: jugador.totalAssists || 0
        };
        setSlots(nuevosSlots);
        setMostrarBuscador(false);
        setSlotActivo(null);
    };

    // Eliminar un jugador de un slot ocupado
    const eliminarJugadorDeSlot = (index, e) => {
        e.stopPropagation(); // Evitamos que abra el buscador al hacer clic en el botón de borrar
        const nuevosSlots = [...slots];
        nuevosSlots[index] = null;
        setSlots(nuevosSlots);
        setResultados(null);
    };

    // =========================================================================
    // 🧠 ALGORITMO CEREBRO: SIMULACIÓN DE CONTEXTO Y PARTICIPACIÓN AL VUELO
    // =========================================================================
    const realizarComparacionContextual = async () => {
        console.log("🚀 INICIANDO COMPARACIÓN CONTEXTUAL REVISADA (CON NUEVOS FILTROS TEMPORALES)");
        console.log("📌 Filtro analítico seleccionado:", filtroContexto);

        setProcesandoCalculos(true);
        const nuevosResultados = [];

        // 1. Lista oficial del Backend (en minúsculas para comparar de forma segura)
        const EQUIPOS_GRANDES = [
            "real madrid", "barcelona", "atlético madrid", "atletico madrid", "athletic bilbao",
            "manchester city", "liverpool", "arsenal", "manchester united", "chelsea", "tottenham hotspur",
            "juventus", "inter milan", "ac milan", "napoli", "roma",
            "bayern munich", "borussia dortmund", "bayer leverkusen",
            "marseille", "lyon", "paris sg", "psg"
        ];

        // Filtramos los slots ocupados de verdad
        const listaAComparar = slots.filter(s => s !== null);
        console.log(`👥 Jugadores a comparar (${listaAComparar.length}):`, listaAComparar.map(j => j.name));

        for (const jugador of listaAComparar) {
            console.log(`\n==================================================`);
            console.log(`🏃‍♂️ PROCESANDO JUGADOR: ${jugador.name} (${jugador.team})`);
            console.log(`==================================================`);

            try {
                const responsePartidos = await api.get(`/matches/all?team=${encodeURIComponent(jugador.team)}`);
                const partidosClub = responsePartidos.data || [];

                const totalPartidosTemporada = partidosClub;
                const partidosJugadosReales = Math.ceil(totalPartidosTemporada.length * 0.9);

                // Arrays para rastrear las aportaciones simuladas de forma independiente
                let partidosConGol = [];
                let partidosConAsistencia = [];
                let partidosConAlgunaAportacion = [];
                let partidosSinNingunaAportacion = [];

                const golesTotales = jugador.totalGoals || 0;
                const asistenciasTotales = jugador.totalAssists || 0;

                totalPartidosTemporada.forEach((partido, index) => {
                    const texto = partido.texto || "";
                    const matchScore = texto.match(/^(.+?)\s+(\d+)\s*-\s*(\d+)\s+(.+)$/);

                    let esLocal = false;
                    let clubGano = false;
                    let equipoRival = "";

                    if (matchScore) {
                        const eqLocal = matchScore[1].trim();
                        const eqVisitante = matchScore[4].trim();
                        const gLocal = parseInt(matchScore[2], 10);
                        const gVisitante = parseInt(matchScore[3], 10);

                        esLocal = eqLocal.toLowerCase().includes(jugador.team.toLowerCase());
                        equipoRival = esLocal ? eqVisitante : eqLocal;

                        if (gLocal === gVisitante) {
                            clubGano = false;
                        } else if (esLocal) {
                            clubGano = gLocal > gVisitante;
                        } else {
                            clubGano = gVisitante > gLocal;
                        }
                    }

                    const miEquipoEsGrande = EQUIPOS_GRANDES.some(granEquipo => jugador.team.toLowerCase().includes(granEquipo));
                    const elRivalEsGrande = EQUIPOS_GRANDES.some(granEquipo => equipoRival.toLowerCase().includes(granEquipo));

                    let esPartidoGrandeReal = miEquipoEsGrande ? elRivalEsGrande : elRivalEsGrande;

                    const metadataPartido = {
                        ...partido,
                        esLocal,
                        clubGano,
                        equipoRival,
                        esPartidoGrande: esPartidoGrandeReal
                    };

                    let tuvoAportacion = false;

                    if (golesTotales > 0 && index < golesTotales) {
                        partidosConGol.push(metadataPartido);
                        tuvoAportacion = true;
                    }

                    if (asistenciasTotales > 0 && index >= 2 && index < (asistenciasTotales + 2)) {
                        partidosConAsistencia.push(metadataPartido);
                        tuvoAportacion = true;
                    }

                    if (tuvoAportacion) {
                        partidosConAlgunaAportacion.push(metadataPartido);
                    } else {
                        partidosSinNingunaAportacion.push(metadataPartido);
                    }
                });

                const partidosFaltantesPorAsignar = Math.max(0, partidosJugadosReales - partidosConAlgunaAportacion.length);
                const partidosSinAportacionMezclados = [...partidosSinNingunaAportacion].sort(() => 0.5 - Math.random());
                const partidosSimuladosParticipados = [
                    ...partidosConAlgunaAportacion,
                    ...partidosSinAportacionMezclados.slice(0, partidosFaltantesPorAsignar)
                ];

                // 5. APLICACIÓN ESTRICTA DEL FILTRO DE CONTEXTO SELECCIONADO
                let partidosFiltrados = [];
                let golesEnContexto = 0;
                let asistenciasEnContexto = 0;

                if (filtroContexto === 'LOCAL') {
                    partidosFiltrados = partidosSimuladosParticipados.filter(p => p.esLocal);
                    golesEnContexto = partidosFiltrados.filter(p => partidosConGol.includes(p)).length;
                    asistenciasEnContexto = partidosFiltrados.filter(p => partidosConAsistencia.includes(p)).length;

                } else if (filtroContexto === 'VISITANTE') {
                    partidosFiltrados = partidosSimuladosParticipados.filter(p => !p.esLocal);
                    golesEnContexto = partidosFiltrados.filter(p => partidosConGol.includes(p)).length;
                    asistenciasEnContexto = partidosFiltrados.filter(p => partidosConAsistencia.includes(p)).length;

                } else if (filtroContexto === 'GRANDES') {
                    partidosFiltrados = partidosSimuladosParticipados.filter(p => p.esPartidoGrande);
                    golesEnContexto = partidosFiltrados.filter(p => partidosConGol.includes(p)).length;
                    asistenciasEnContexto = partidosFiltrados.filter(p => partidosConAsistencia.includes(p)).length;

                } else if (filtroContexto === 'MINUTOS_FINALES') {
                    const factorParticipacionFinal = Math.random() * (0.95 - 0.70) + 0.70;
                    const cantidadPartidosEnTramoFinal = Math.ceil(partidosSimuladosParticipados.length * factorParticipacionFinal);
                    
                    partidosFiltrados = [...partidosSimuladosParticipados]
                        .sort(() => 0.5 - Math.random())
                        .slice(0, cantidadPartidosEnTramoFinal);

                    golesEnContexto = partidosFiltrados.filter(p => partidosConGol.includes(p) && partidosSimuladosParticipados.indexOf(p) % 4 === 0).length;
                    asistenciasEnContexto = partidosFiltrados.filter(p => partidosConAsistencia.includes(p) && partidosSimuladosParticipados.indexOf(p) % 4 === 0).length;

                // ⚡ FILTRO: ENCHUFADO DESDE EL INICIO (0' - 15')
                } else if (filtroContexto === 'ENCHUFADO_INICIO') {
                    const factorPresencia = Math.random() * (0.98 - 0.90) + 0.90;
                    const cantidadPartidos = Math.ceil(partidosSimuladosParticipados.length * factorPresencia);
                    partidosFiltrados = [...partidosSimuladosParticipados].sort(() => 0.5 - Math.random()).slice(0, cantidadPartidos);

                    golesEnContexto = partidosFiltrados.filter(p => partidosConGol.includes(p) && partidosSimuladosParticipados.indexOf(p) % 5 === 0).length;
                    asistenciasEnContexto = partidosFiltrados.filter(p => partidosConAsistencia.includes(p) && partidosSimuladosParticipados.indexOf(p) % 5 === 0).length;

                // 🏃‍♂️ FILTRO: APRETANDO HASTA EL DESCANSO (30' - 45')
                } else if (filtroContexto === 'APRETANDO_DESCANSO') {
                    const factorPresencia = Math.random() * (0.92 - 0.75) + 0.75;
                    const cantidadPartidos = Math.ceil(partidosSimuladosParticipados.length * factorPresencia);
                    partidosFiltrados = [...partidosSimuladosParticipados].sort(() => 0.5 - Math.random()).slice(0, cantidadPartidos);

                    golesEnContexto = partidosFiltrados.filter(p => partidosConGol.includes(p) && partidosSimuladosParticipados.indexOf(p) % 5 === 1).length;
                    asistenciasEnContexto = partidosFiltrados.filter(p => partidosConAsistencia.includes(p) && partidosSimuladosParticipados.indexOf(p) % 5 === 1).length;

                // 🧠 FILTRO: SIRVIERON LAS INDICACIONES (45' - 60')
                } else if (filtroContexto === 'INDICACIONES_ENTRENADOR') {
                    const factorPresencia = Math.random() * (0.90 - 0.70) + 0.70;
                    const cantidadPartidos = Math.ceil(partidosSimuladosParticipados.length * factorPresencia);
                    partidosFiltrados = [...partidosSimuladosParticipados].sort(() => 0.5 - Math.random()).slice(0, cantidadPartidos);

                    golesEnContexto = partidosFiltrados.filter(p => partidosConGol.includes(p) && partidosSimuladosParticipados.indexOf(p) % 5 === 2).length;
                    asistenciasEnContexto = partidosFiltrados.filter(p => partidosConAsistencia.includes(p) && partidosSimuladosParticipados.indexOf(p) % 5 === 2).length;
                }

                const totalGaeContexto = golesEnContexto + asistenciasEnContexto;
                const victoriasEnContexto = partidosFiltrados.filter(p => p.clubGano).length;
                const partidosJugadosContexto = partidosFiltrados.length || 1;

                const promedioGoles = (golesEnContexto / partidosJugadosContexto).toFixed(2);
                const promedioAsistencias = (asistenciasEnContexto / partidosJugadosContexto).toFixed(2);
                const promedioGa = (totalGaeContexto / partidosJugadosContexto).toFixed(2);

                nuevosResultados.push({
                    nombre: jugador.name,
                    club: jugador.team,
                    posicion: jugador.position,
                    partidosContexto: partidosFiltrados.length,
                    golesContexto: golesEnContexto,
                    promedio: promedioGoles,
                    asistenciasContexto: asistenciasEnContexto,
                    promedioAsistencias: promedioAsistencias,
                    gaContexto: totalGaeContexto,
                    promedioGa: promedioGa,
                    victorias: victoriasEnContexto
                });

            } catch (error) {
                console.error(`❌ Error calculando analítica para ${jugador.name}:`, error);
            }
        }

        setResultados(nuevosResultados);
        setProcesandoCalculos(false);
    };

    const jugadoresFiltradosSubBuscador = jugadoresBusqueda.filter(j =>
        j.name.toLowerCase().includes(busqueda.toLowerCase()) ||
        j.team.toLowerCase().includes(busqueda.toLowerCase())
    );

    // Mapeo amigable para los títulos de los contextos en la cabecera de la tarjeta
    const nombresFormatosFiltros = {
        'LOCAL': 'Rendimiento de Local',
        'VISITANTE': 'Rendimiento de Visitante',
        'GRANDES': 'Partidos Grandes',
        'MINUTOS_FINALES': 'Minutos Finales (75\' - 90\')',
        'ENCHUFADO_INICIO': 'Enchufado desde el Inicio (0\' - 15\')',
        'APRETANDO_DESCANSO': 'Apretando hasta el Descanso (30\' - 45\')',
        'INDICACIONES_ENTRENADOR': 'Sirvieron las Indicaciones (45\' - 60\')'
    };

    return (
        <div className="modulo-comparacion-container">

            {/* RENDERIZADO DE LAS 4 RANURAS EN FILA */}
            <div className="fila-slots-comparacion">
                {slots.map((jugador, index) => (
                    <div
                        key={index}
                        className={`slot-card ${jugador ? 'slot-ocupado' : 'slot-vacio'}`}
                        onClick={() => !jugador && abrirBuscadorParaSlot(index)}
                    >
                        {jugador ? (
                            <div className="info-jugador-slot">
                                <button className="btn-eliminar-slot" onClick={(e) => eliminarJugadorDeSlot(index, e)}>❌</button>
                                <div className="avatar-slot">{jugador.name.charAt(0).toUpperCase()}</div>
                                <h4>{jugador.name}</h4>
                                <p>{jugador.team}</p>
                                <span className="badge-posicion-slot">{jugador.position}</span>
                            </div>
                        ) : (
                            <div className="crear-slot-prompt">
                                <span className="cruz-roja-add">❌</span>
                                <p>Seleccionar Jugador</p>
                            </div>
                        )}
                    </div>
                ))}
            </div>

            {/* SECCIÓN DEL SUB-BUSCADOR */}
            {mostrarBuscador && (
                <div className="sub-buscador-modal-box">
                    <h3>➕ Fichar Jugador para la Ranura #{slotActivo + 1}</h3>
                    <input
                        type="text"
                        className="search-input-sub"
                        placeholder="Teclea el nombre del futbolista o club..."
                        value={busqueda}
                        onChange={(e) => setBusqueda(e.target.value)}
                    />

                    {busqueda.trim() !== '' && (
                        <div className="tabla-resultados-sub-wrapper">
                            <table className="tabla-jugadores-sub">
                                <thead>
                                    <tr>
                                        <th>Nombre</th>
                                        <th>Club</th>
                                        <th>Acción</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {jugadoresFiltradosSubBuscador.slice(0, 6).map(j => (
                                        <tr key={j.id}>
                                            <td><strong>{j.name}</strong></td>
                                            <td>{j.team}</td>
                                            <td>
                                                <button className="btn-add-to-comp" onClick={() => agregarJugadorASlot(j)}>
                                                    ➕ Añadir
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                    <button className="btn-cerrar-sub-buscador" onClick={() => setMostrarBuscador(false)}>Cancelar</button>
                </div>
            )}

            {/* SELECTOR DE FILTRO DE CONTEXTO */}
            <div className="control-filtro-comparar-box">
                <label className="label-selector-contexto">Seleccionar Módulo de Comparación:</label>
                <select
                    className="select-filtro-contextual"
                    value={filtroContexto}
                    onChange={(e) => setFiltroContexto(e.target.value)}
                    disabled={jugadoresSeleccionadosCount < 2}
                >
                    <option value="">-- Selecciona un filtro analítico --</option>
                    <option value="LOCAL">🏠 Rendimiento de Local</option>
                    <option value="VISITANTE">✈️ Rendimiento de Visitante</option>
                    <option value="GRANDES">🔥 Rendimiento en Partidos Grandes</option>
                    <option value="ENCHUFADO_INICIO">⚡ Enchufado desde el inicio (0' - 15')</option>
                    <option value="APRETANDO_DESCANSO">🏃‍♂️ Apretando hasta el descanso (30' - 45')</option>
                    <option value="INDICACIONES_ENTRENADOR">🧠 Sirvieron las indicaciones (45' - 60')</option>
                    <option value="MINUTOS_FINALES">⏱️ Hasta el final (Últimos 15')</option>
                </select>

                <button
                    className="btn-ejecutar-comparacion"
                    disabled={!botonCompararDesbloqueado || procesandoCalculos}
                    onClick={realizarComparacionContextual}
                >
                    {procesandoCalculos ? 'Procesando Estadísticas...' : '📊 Realizar Comparación'}
                </button>
            </div>

            {/* NUEVA ESTRUCTURA DE TARJETA DE RENDIMIENTO CONTINUA POR BARRAS */}
            {resultados && resultados.length > 0 && (
                <div className="bloque-resultados-analisis-box">
                    <h3>📊 Comparativa de Rendimiento Contextual ({nombresFormatosFiltros[filtroContexto] || filtroContexto})</h3>

                    {/* Fila de Cabecera Dinámica (Columnas adaptables según nº de jugadores) */}
                    <div className="tabla-contextual-header-dinamica" style={{ '--total-jugadores': resultados.length }}>
                        {resultados.map((res, i) => (
                            <div key={i} className="columna-header-jugador">
                                <span className="nombre-header">{res.nombre}</span>
                                <span className="club-header">{res.club}</span>
                            </div>
                        ))}
                    </div>

                    {/* CONTENEDOR DE FILAS ESTADÍSTICAS */}
                    <div className="contenedor-filas-estadisticas">

                        {[
                            { etiqueta: "Partidos Jugados", clave: "partidosContexto" },
                            { etiqueta: "Goles Anotados", clave: "golesContexto" },
                            { etiqueta: "Promedio Goles", clave: "promedio" },
                            { etiqueta: "Asistencias", clave: "asistenciasContexto" },
                            { etiqueta: "Promedio Asistencias", clave: "promedioAsistencias" },
                            { etiqueta: "Total G/A", clave: "gaContexto" },
                            { etiqueta: "Promedio G/A", clave: "promedioGa" },
                            { etiqueta: "Victorias del Equipo", clave: "victorias" }
                        ].map((metrica, idx) => {
                            const valores = resultados.map(r => parseFloat(r[metrica.clave]) || 0);
                            const sumaTotal = valores.reduce((acc, curr) => acc + curr, 0);

                            // 1. Encontramos el valor máximo de esta fila para el resaltado
                            const valorMaximo = Math.max(...valores);

                            return (
                                <div key={idx} className="fila-estadistica-item">
                                    <div className="etiqueta-metrica-titulo">{metrica.etiqueta}</div>

                                    <div className="valores-numericos-grid" style={{ '--total-jugadores': resultados.length }}>
                                        {resultados.map((res, i) => {
                                            const valorActual = parseFloat(res[metrica.clave]) || 0;

                                            // Evalúa si es el ganador de la fila (y nos aseguramos de que no sean todos 0)
                                            const esMax = valorActual === valorMaximo && valorMaximo > 0;

                                            return (
                                                <div
                                                    key={i}
                                                    className={`valor-jugador-numero ${esMax ? 'es-maximo' : ''}`}
                                                >
                                                    {res[metrica.clave]}
                                                    {esMax && <span className="corona-max">⭐</span>}
                                                </div>
                                            );
                                        })}
                                    </div>

                                    {/* BARRA DE PROPORCIÓN CONTINUA */}
                                    <div className="barra-proporcion-continua-wrapper">
                                        {resultados.map((res, i) => {
                                            const valorActual = parseFloat(res[metrica.clave]) || 0;
                                            let porcentajeAncho = 100 / resultados.length;

                                            if (sumaTotal > 0) {
                                                porcentajeAncho = (valorActual / sumaTotal) * 100;
                                            }

                                            return (
                                                <div
                                                    key={i}
                                                    className={`segmento-barra-jugador jugador-index-${i}`}
                                                    style={{
                                                        width: `${porcentajeAncho}%`,
                                                        opacity: valorActual === 0 && sumaTotal > 0 ? 0.15 : 1
                                                    }}
                                                />
                                            );
                                        })}
                                    </div>
                                </div>
                            );
                        })}

                    </div>
                </div>
            )}

            {/* BOTONERA HOME */}
            <div className="botonera-navegacion-inferior" style={{ marginTop: '30px' }}>
                <button onClick={onVolverAlInicio} className="btn-nav-icono" title="Volver al menú de inicio">
                    🏠
                </button>
            </div>

        </div>
    );
}

export default ModuloComparacion;