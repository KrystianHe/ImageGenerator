import React from 'react';
import { Container, Row, Col, Card, Button } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import Navigation from '../components/Navigation';

const HomePage = () => {
  return (
    <>
      <Navigation />
      <Container className="py-5">
        <Row className="mb-4">
          <Col className="text-center">
            <h1>Witaj w Generatorze Obrazów AI</h1>
            <p className="lead">
              Odkryj moc sztucznej inteligencji do generowania obrazów i analizy tekstu
            </p>
          </Col>
        </Row>
        
        <Row className="justify-content-center">
          <Col md={4} className="mb-4">
            <Card className="h-100">
              <Card.Body>
                <Card.Title>Analiza Tekstu</Card.Title>
                <Card.Text>
                  Analizuj tekst i sprawdź jego wydźwięk, kategorie i wyodrębnij kluczowe informacje.
                </Card.Text>
                <Button as={Link} to="/text-analyzer" variant="primary">Wypróbuj</Button>
              </Card.Body>
            </Card>
          </Col>
          
          <Col md={4} className="mb-4">
            <Card className="h-100">
              <Card.Body>
                <Card.Title>Generator Obrazów</Card.Title>
                <Card.Text>
                  Twórz obrazy na podstawie opisu tekstowego korzystając z zaawansowanych modeli AI.
                </Card.Text>
                <Button as={Link} to="/image-generator" variant="primary">Wypróbuj</Button>
              </Card.Body>
            </Card>
          </Col>
          
          <Col md={4} className="mb-4">
            <Card className="h-100">
              <Card.Body>
                <Card.Title>Generator GIF</Card.Title>
                <Card.Text>
                  Stwórz animowane GIF-y na podstawie Twoich pomysłów i opisów.
                </Card.Text>
                <Button as={Link} to="/gif-generator" variant="primary">Wypróbuj</Button>
              </Card.Body>
            </Card>
          </Col>
        </Row>
        
        <Row className="mt-4 justify-content-center">
          <Col md={8} className="text-center">
            <Card>
              <Card.Body>
                <Card.Title>Galeria</Card.Title>
                <Card.Text>
                  Zobacz wygenerowane przez społeczność obrazy i podziel się swoimi kreacjami.
                </Card.Text>
                <Button as={Link} to="/gallery" variant="outline-primary">Przejdź do Galerii</Button>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>
    </>
  );
};

export default HomePage; 