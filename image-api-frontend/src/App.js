import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import './App.css';

// Importujemy komponenty stron
import HomePage from './pages/HomePage';
import TextAnalyzer from './pages/TextAnalyzer';
import ImageGenerator from './pages/ImageGenerator';
import GifGenerator from './pages/GifGenerator';
import Gallery from './pages/Gallery';

const App = () => {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/image-generator" element={<ImageGenerator />} />
          <Route path="/text-analyzer" element={<TextAnalyzer />} />
          <Route path="/gif-generator" element={<GifGenerator />} />
          <Route path="/gallery" element={<Gallery />} />
        </Routes>
      </div>
    </Router>
  );
};

export default App;
