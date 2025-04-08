import React, { useState } from 'react';
import { Container, Form, Button, Card, Spinner } from 'react-bootstrap';
import Navigation from '../components/Navigation';

const TextAnalyzer = () => {
  const [text, setText] = useState('');
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    // Tutaj w przyszłości będzie integracja z API
    // Symulacja opóźnienia dla demonstracji
    setTimeout(() => {
      setResult({
        sentiment: 'Pozytywny',
        categories: ['Technologia', 'AI'],
        entities: ['Generator', 'AI', 'Obraz'],
        keywords: ['sztuczna inteligencja', 'generowanie', 'analiza']
      });
      setLoading(false);
    }, 1500);
  };

  return (
    <>
      <Navigation />
      <Container className="py-5">
        <h1 className="text-center mb-4">Analiza Tekstu</h1>
        
        <Card className="mb-4">
          <Card.Body>
            <Form onSubmit={handleSubmit}>
              <Form.Group className="mb-3">
                <Form.Label>Wpisz tekst do analizy</Form.Label>
                <Form.Control
                  as="textarea"
                  rows={5}
                  value={text}
                  onChange={(e) => setText(e.target.value)}
                  placeholder="Wprowadź tekst do analizy..."
                  required
                />
              </Form.Group>
              <Button 
                variant="primary" 
                type="submit" 
                disabled={loading || !text.trim()}
              >
                {loading ? (
                  <>
                    <Spinner
                      as="span"
                      animation="border"
                      size="sm"
                      role="status"
                      aria-hidden="true"
                      className="me-2"
                    />
                    Analizowanie...
                  </>
                ) : (
                  'Analizuj Tekst'
                )}
              </Button>
            </Form>
          </Card.Body>
        </Card>
        
        {result && (
          <Card>
            <Card.Header>Wyniki Analizy</Card.Header>
            <Card.Body>
              <div className="mb-3">
                <h5>Wydźwięk:</h5>
                <p>{result.sentiment}</p>
              </div>
              
              <div className="mb-3">
                <h5>Kategorie:</h5>
                <ul>
                  {result.categories.map((category, index) => (
                    <li key={index}>{category}</li>
                  ))}
                </ul>
              </div>
              
              <div className="mb-3">
                <h5>Rozpoznane Encje:</h5>
                <ul>
                  {result.entities.map((entity, index) => (
                    <li key={index}>{entity}</li>
                  ))}
                </ul>
              </div>
              
              <div>
                <h5>Słowa Kluczowe:</h5>
                <div>
                  {result.keywords.map((keyword, index) => (
                    <span 
                      key={index} 
                      className="badge bg-primary me-2 mb-2"
                    >
                      {keyword}
                    </span>
                  ))}
                </div>
              </div>
            </Card.Body>
          </Card>
        )}
      </Container>
    </>
  );
};

export default TextAnalyzer; 