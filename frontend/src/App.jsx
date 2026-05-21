import React, { useState } from 'react';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import './components/css/App.css';

function App() {
  const [vista, setVista] = useState('MENU');
  const [usuario, setUsuario] = useState(null);

  const continuarComoInvitado = () => {
    setUsuario({ nombre: 'Invitado', rol: 'GUEST' });
    setVista('DASHBOARD');
  };

  return (
    <div className="menu-container">

      {/* 1. PANTALLA DE BIENVENIDA */}
      {vista === 'MENU' && (
        <div className="menu-welcome">
          <h1 className="menu-title">Sistema de Estadísticas Deportivas</h1>

          <div className="menu-button-group">
            <button onClick={() => setVista('LOGIN')} className="btn-menu btn-menu-primary">
              Iniciar Sesión
            </button>
            <button onClick={() => setVista('REGISTER')} className="btn-menu btn-menu-secondary">
              Registrarse
            </button>
            <button onClick={continuarComoInvitado} className="btn-menu btn-menu-link">
              Continuar sin iniciar sesión (Invitado)
            </button>
          </div>
        </div>
      )}

      {/* 2. COMPONENTE DE LOGIN */}
      {vista === 'LOGIN' && (
        <Login
          onBack={() => setVista('MENU')}
          onLoginSuccess={(user) => { setUsuario(user); setVista('DASHBOARD'); }}
        />
      )}

      {/* 3. COMPONENTE DE REGISTRO */}
      {vista === 'REGISTER' && (
        <Register
          onBack={() => setVista('MENU')}
          onRegisterSuccess={() => setVista('LOGIN')}
        />
      )}

      {/* 4. COMPONENTE PRINCIPAL (LOGUEADO O INVITADO) */}
      {vista === 'DASHBOARD' && (
        <Dashboard
          usuario={usuario}
          onLogout={() => { setUsuario(null); setVista('MENU'); }}
        />
      )}

    </div>
  );
}

export default App;