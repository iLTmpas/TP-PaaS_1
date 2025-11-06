import React, { useState, useRef, useEffect } from 'react';
import './App.css';

function App() {
  const students = [
    { id: 1, prenom: 'Alice', nom: 'Dupont', classe: '1A' },
    { id: 2, prenom: 'Lucas', nom: 'Martin', classe: '2B' },
    { id: 3, prenom: 'Maya', nom: 'Bernard', classe: '1A' },
    { id: 4, prenom: 'Noah', nom: 'Petit', classe: '3C' }
  ];

  // bouton qui devient vert pendant 2 secondes
  const [btnActive, setBtnActive] = useState(false);
  const timerRef = useRef(null);

  const handleButtonClick = () => {
    // activer
    setBtnActive(true);
    // réinitialiser timer s'il existe
    if (timerRef.current) {
      clearTimeout(timerRef.current);
    }
    timerRef.current = setTimeout(() => {
      setBtnActive(false);
      timerRef.current = null;
    }, 2000);
  };

  useEffect(() => {
    return () => {
      // cleanup si le composant est démonté
      if (timerRef.current) clearTimeout(timerRef.current);
    };
  }, []);

  return (
    <div className="App">
      <header className="App-header">
        <h1>Liste des élèves</h1>

        <div className="table-wrapper">
          <table className="students-table">
            <thead>
              <tr>
                <th>Id</th>
                <th>Prénom</th>
                <th>Nom</th>
                <th>Classe</th>
              </tr>
            </thead>
            <tbody>
              {students.map((s) => (
                <tr key={s.id}>
                  <td>{s.id}</td>
                  <td>{s.prenom}</td>
                  <td>{s.nom}</td>
                  <td>{s.classe}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="controls">
          <button
            type="button"
            className={`blink-btn ${btnActive ? 'blink-btn--active' : ''}`}
            onClick={handleButtonClick}
            aria-pressed={btnActive}
            aria-disabled={btnActive}
            disabled={btnActive}
          >
            Ouvrir la porte
          </button>
        </div>
      </header>
    </div>
  );
}

export default App;
