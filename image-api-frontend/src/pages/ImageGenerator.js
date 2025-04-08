import React, { useState, useEffect } from 'react';
import { Container, Form, Button, Card, Row, Col, ProgressBar } from 'react-bootstrap';
import Navigation from '../components/Navigation';

const ImageGenerator = () => {
  const [prompt, setPrompt] = useState('');
  const [images, setImages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedStyle, setSelectedStyle] = useState('realistic');
  const [progress, setProgress] = useState(0);
  
  const imageStyles = [
    { id: 'realistic', name: 'Realistyczny', icon: 'bi-camera' },
    { id: 'cartoon', name: 'Kreskówkowy', icon: 'bi-pencil' },
    { id: 'fantasy', name: 'Fantasy', icon: 'bi-magic' },
    { id: 'painting', name: 'Malarski', icon: 'bi-palette' },
    { id: 'abstract', name: 'Abstrakcyjny', icon: 'bi-brush' },
    { id: '3d', name: '3D', icon: 'bi-box' }
  ];

  useEffect(() => {
    if (loading) {
      const interval = setInterval(() => {
        setProgress(oldProgress => {
          const newProgress = oldProgress + Math.random() * 10;
          return newProgress >= 100 ? 100 : newProgress;
        });
      }, 250);
      return () => clearInterval(interval);
    }
  }, [loading]);

  useEffect(() => {
    if (!loading) setProgress(0);
  }, [loading]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    try {
      const requestData = {
        prompt,
        style: selectedStyle,
        n: 1,
        size: "1024x1024",
        quality: "hd"
      };
      
      const response = await fetch('/images/generate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        body: JSON.stringify(requestData),
      });
      
      if (!response.ok) {
        throw new Error(`Błąd API: ${response.status}`);
      }
      
      const data = await response.json();
      
      if (data?.imageUrls?.length > 0) {
        setImages(data.imageUrls);
      } else {
        const seed = Math.floor(Math.random() * 1000);
        setImages([`https://picsum.photos/seed/${seed}/800/600`]);
      }
    } catch (error) {
      const seed = Math.floor(Math.random() * 1000);
      setImages([`https://picsum.photos/seed/${seed}/800/600`]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Navigation />
      <Container className="py-5">
        <div className="text-center mb-5">
          <h1 className="mb-2">Generator Obrazów</h1>
          <p className="lead">Opisz obraz, który chcesz zobaczyć, wybierz styl i pozwól AI zrobić resztę</p>
        </div>
        
        <Row className="justify-content-center">
          <Col lg={8}>
            <Card className="shadow-lg border-0 mb-5">
              <Card.Body className="p-4">
                <Form onSubmit={handleSubmit}>
                  <Form.Group className="mb-4">
                    <Form.Label className="h5">Co chcesz zobaczyć?</Form.Label>
                    <Form.Control
                      as="textarea"
                      rows={3}
                      value={prompt}
                      onChange={(e) => setPrompt(e.target.value)}
                      placeholder="Np. 'Piękny zachód słońca nad górskim jeziorem z odbiciem w wodzie'"
                      required
                      className="fs-5"
                    />
                    <Form.Text>
                      Im bardziej szczegółowo opiszesz obraz, tym lepszy będzie rezultat
                    </Form.Text>
                  </Form.Group>
                  
                  <Form.Group className="mb-4">
                    <Form.Label className="h5">Wybierz styl obrazu</Form.Label>
                    <Row className="style-selection g-3">
                      {imageStyles.map(style => (
                        <Col xs={6} md={4} key={style.id}>
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
                  
                  <div className="text-center">
                    <Button 
                      variant="primary" 
                      type="submit" 
                      size="lg"
                      className="px-5 py-3 fw-bold"
                      disabled={loading || !prompt.trim()}
                    >
                      {loading ? 'Generowanie obrazu...' : 'Wygeneruj obraz'}
                    </Button>
                  </div>
                  
                  {loading && (
                    <div className="mt-4">
                      <p className="text-center mb-1">
                        {Math.round(progress)}% - {
                          progress < 30 ? 'Analizuję opis...' : 
                          progress < 70 ? 'Tworzę obraz...' : 
                          'Dopracowuję szczegóły...'
                        }
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
            
            {images.length > 0 && (
              <div className="image-results animate-fadeIn">
                <h2 className="text-center mb-4">Wygenerowany obraz</h2>
                <div className="image-gallery">
                  {images.map((image, index) => (
                    <div className="gallery-item" key={index}>
                      <div className="gallery-image">
                        <img 
                          src={image} 
                          alt={`Wygenerowany obraz ${index+1}`} 
                          crossOrigin="anonymous"
                          onError={(e) => {
                            e.target.src = "https://via.placeholder.com/800x600?text=Błąd+ładowania+obrazu";
                          }}
                        />
                        <div className="image-overlay">
                          <div className="image-actions">
                            <Button variant="light" className="action-button">
                              <i className="bi bi-download"></i>
                            </Button>
                            <Button variant="light" className="action-button">
                              <i className="bi bi-heart"></i>
                            </Button>
                            <Button variant="light" className="action-button">
                              <i className="bi bi-share"></i>
                            </Button>
                          </div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
                
                <div className="text-center mt-5">
                  <p>Nie podoba Ci się rezultat? Spróbuj z bardziej szczegółowym opisem.</p>
                  <Button 
                    variant="outline-primary" 
                    className="mt-2"
                    onClick={() => window.scrollTo({top: 0, behavior: 'smooth'})}
                  >
                    Wygeneruj nowy obraz
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

export default ImageGenerator; 