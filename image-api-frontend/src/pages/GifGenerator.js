import React, { useState, useEffect } from 'react';
import { Container, Form, Button, Card, Row, Col, ProgressBar } from 'react-bootstrap';
import Navigation from '../components/Navigation';

const GifGenerator = () => {
  const [prompt, setPrompt] = useState('');
  const [gif, setGif] = useState(null);
  const [loading, setLoading] = useState(false);
  const [animationSpeed, setAnimationSpeed] = useState('normal');
  const [selectedStyle, setSelectedStyle] = useState('cartoon');
  const [progress, setProgress] = useState(0);

  const gifStyles = [
    { id: 'cartoon', name: 'Kreskówkowy', icon: 'bi-emoji-smile' },
    { id: 'realistic', name: 'Realistyczny', icon: 'bi-camera-video' },
    { id: 'pixel', name: 'Pikselowy', icon: 'bi-grid-3x3' },
    { id: 'anime', name: 'Anime', icon: 'bi-stars' }
  ];
  
  const speedOptions = [
    { id: 'slow', name: 'Wolno', factor: 2 },
    { id: 'normal', name: 'Normalnie', factor: 1 },
    { id: 'fast', name: 'Szybko', factor: 0.5 }
  ];

  // Symulacja procesu generowania GIF
  useEffect(() => {
    if (loading) {
      const interval = setInterval(() => {
        setProgress(oldProgress => {
          const newProgress = oldProgress + Math.random() * 8;
          if (newProgress >= 100) {
            clearInterval(interval);
            return 100;
          }
          return newProgress;
        });
      }, 300);

      return () => clearInterval(interval);
    }
  }, [loading]);

  // Reset postępu generowania
  useEffect(() => {
    if (!loading) {
      setProgress(0);
    }
  }, [loading]);

  const handleSubmit = (e) => {
    e.preventDefault();
    setLoading(true);
    
    // Symulacja odpowiedzi z API
    setTimeout(() => {
      // Wybór przykładowego GIF-a na podstawie stylu
      let gifUrl;
      
      switch(selectedStyle) {
        case 'cartoon':
          gifUrl = 'https://media.giphy.com/media/3o7TKsR3iQUr5gyra8/giphy.gif';
          break;
        case 'realistic':
          gifUrl = 'https://media.giphy.com/media/26tn33aiTi1jkl6H6/giphy.gif';
          break;
        case 'pixel':
          gifUrl = 'https://media.giphy.com/media/1BhGzgpZoIqzSRoiLm/giphy.gif';
          break;
        case 'anime':
          gifUrl = 'https://media.giphy.com/media/xUOrwpPFzqDh48XEek/giphy.gif';
          break;
        default:
          gifUrl = 'https://media.giphy.com/media/3o7TKsR3iQUr5gyra8/giphy.gif';
      }
      
      setGif(gifUrl);
      setLoading(false);
    }, 3000);
  };

  const getSpeedFactor = () => {
    const option = speedOptions.find(opt => opt.id === animationSpeed);
    return option ? option.factor : 1;
  };

  return (
    <>
      <Navigation />
      <Container className="py-5">
        <div className="text-center mb-5">
          <h1 className="mb-2">Generator GIF</h1>
          <p className="lead">Opisz animację, którą chcesz stworzyć, i pozwól magii AI się zdarzyć</p>
        </div>
        
        <Row className="justify-content-center">
          <Col lg={8}>
            <Card className="shadow-lg border-0 mb-5">
              <Card.Body className="p-4">
                <Form onSubmit={handleSubmit}>
                  <Form.Group className="mb-4">
                    <Form.Label className="h5">Opisz animację GIF</Form.Label>
                    <Form.Control
                      as="textarea"
                      rows={3}
                      value={prompt}
                      onChange={(e) => setPrompt(e.target.value)}
                      placeholder="Np. 'Mały kotek skaczący przez obręcz tęczy'"
                      required
                      className="fs-5"
                    />
                    <Form.Text>
                      Opisz dokładnie, co chcesz zobaczyć w animacji
                    </Form.Text>
                  </Form.Group>
                  
                  <Row className="mb-4">
                    <Col md={6}>
                      <Form.Group>
                        <Form.Label className="h5">Styl animacji</Form.Label>
                        <Row className="style-selection g-2">
                          {gifStyles.map(style => (
                            <Col xs={6} key={style.id}>
                              <div 
                                className={`style-card ${selectedStyle === style.id ? 'selected' : ''}`}
                                onClick={() => setSelectedStyle(style.id)}
                              >
                                <i className={`bi ${style.icon}`}></i>
                                <span>{style.name}</span>
                              </div>
                            </Col>
                          ))}
                        </Row>
                      </Form.Group>
                    </Col>
                    
                    <Col md={6}>
                      <Form.Group>
                        <Form.Label className="h5">Szybkość animacji</Form.Label>
                        <div className="speed-options">
                          {speedOptions.map(option => (
                            <Button
                              key={option.id}
                              variant={animationSpeed === option.id ? 'primary' : 'outline-secondary'}
                              className="speed-option"
                              onClick={() => setAnimationSpeed(option.id)}
                            >
                              {option.name}
                            </Button>
                          ))}
                        </div>
                        <Form.Text>
                          Wybierz, jak szybko będzie odtwarzana animacja
                        </Form.Text>
                      </Form.Group>
                    </Col>
                  </Row>
                  
                  <div className="text-center">
                    <Button 
                      variant="primary" 
                      type="submit" 
                      size="lg"
                      className="px-5 py-3 fw-bold"
                      disabled={loading || !prompt.trim()}
                    >
                      {loading ? 'Generowanie animacji...' : 'Wygeneruj GIF'}
                    </Button>
                  </div>
                  
                  {loading && (
                    <div className="mt-4">
                      <p className="text-center mb-1">
                        {Math.round(progress)}% - {progress < 25 ? 'Analizuję opis...' : 
                          progress < 50 ? 'Tworzę klatki...' : 
                          progress < 75 ? 'Animuję sekwencję...' : 'Optymalizuję GIF...'}
                      </p>
                      <ProgressBar 
                        animated 
                        now={progress} 
                        variant="info"
                        className="generation-progress"
                      />
                    </div>
                  )}
                </Form>
              </Card.Body>
            </Card>
            
            {gif && (
              <div className="gif-result animate-fadeIn">
                <h2 className="text-center mb-4">Twój wygenerowany GIF</h2>
                
                <div className="gif-preview-container">
                  <div className="gif-preview">
                    <img 
                      src={gif} 
                      alt="Wygenerowany GIF" 
                      style={{ 
                        animationDuration: `${getSpeedFactor()}s`,
                        animationPlayState: 'running'
                      }} 
                    />
                  </div>
                  
                  <div className="gif-controls mt-4 text-center">
                    <div className="speed-control-label mb-2">
                      Dostosuj szybkość:
                    </div>
                    <div className="speed-control-buttons">
                      {speedOptions.map(option => (
                        <Button
                          key={option.id}
                          variant={animationSpeed === option.id ? 'primary' : 'outline-secondary'}
                          className="me-2"
                          onClick={() => setAnimationSpeed(option.id)}
                        >
                          {option.name}
                        </Button>
                      ))}
                    </div>
                  </div>
                  
                  <div className="gif-actions mt-4 text-center">
                    <Button variant="outline-primary" className="me-3">
                      <i className="bi bi-download me-2"></i>
                      Pobierz GIF
                    </Button>
                    <Button variant="outline-success">
                      <i className="bi bi-share me-2"></i>
                      Udostępnij
                    </Button>
                  </div>
                </div>
                
                <div className="text-center mt-5">
                  <p>Chcesz stworzyć inną animację?</p>
                  <Button 
                    variant="outline-primary" 
                    className="mt-2"
                    onClick={() => window.scrollTo({top: 0, behavior: 'smooth'})}
                  >
                    Wygeneruj nowy GIF
                  </Button>
                </div>
              </div>
            )}
          </Col>
        </Row>
      </Container>
    </>
  );
};

export default GifGenerator; 