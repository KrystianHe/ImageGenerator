import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Form, InputGroup } from 'react-bootstrap';
import Navigation from '../components/Navigation';

const Gallery = () => {
  const [gallery, setGallery] = useState([]);
  const [filter, setFilter] = useState('');

  useEffect(() => {
    // Tutaj w przyszłości byłoby pobieranie danych z API
    const mockGallery = [
      {
        id: 1,
        title: 'Górski krajobraz',
        type: 'image',
        url: 'https://source.unsplash.com/random/800x600?mountains',
        creator: 'Jan Kowalski',
        createdAt: '2023-10-15'
      },
      {
        id: 2,
        title: 'Futurystyczne miasto',
        type: 'image',
        url: 'https://source.unsplash.com/random/800x600?city',
        creator: 'Anna Nowak',
        createdAt: '2023-10-14'
      },
      {
        id: 3,
        title: 'Podwodny świat',
        type: 'image',
        url: 'https://source.unsplash.com/random/800x600?underwater',
        creator: 'Piotr Wiśniewski',
        createdAt: '2023-10-12'
      },
      {
        id: 4,
        title: 'Zachód słońca',
        type: 'image',
        url: 'https://source.unsplash.com/random/800x600?sunset',
        creator: 'Monika Lis',
        createdAt: '2023-10-10'
      },
      {
        id: 5,
        title: 'Kosmiczna podróż',
        type: 'gif',
        url: 'https://media.giphy.com/media/3o7TKsR3iQUr5gyra8/giphy.gif',
        creator: 'Adam Kowalczyk',
        createdAt: '2023-10-09'
      },
      {
        id: 6,
        title: 'Ruch uliczny',
        type: 'gif',
        url: 'https://media.giphy.com/media/l378sOvgHwmw2n8pG/giphy.gif',
        creator: 'Karolina Zielińska',
        createdAt: '2023-10-08'
      }
    ];
    
    setGallery(mockGallery);
  }, []);

  const filteredGallery = gallery.filter(item => 
    item.title.toLowerCase().includes(filter.toLowerCase()) ||
    item.creator.toLowerCase().includes(filter.toLowerCase())
  );

  return (
    <>
      <Navigation />
      <Container className="py-5">
        <h1 className="text-center mb-4">Galeria</h1>
        
        <Card className="mb-4">
          <Card.Body>
            <Form>
              <InputGroup>
                <Form.Control
                  placeholder="Szukaj w galerii..."
                  value={filter}
                  onChange={(e) => setFilter(e.target.value)}
                />
                <Button variant="outline-secondary">
                  Wyczyść
                </Button>
              </InputGroup>
              
              <div className="mt-3">
                <Form.Group className="d-flex">
                  <Form.Label className="me-3">Filtruj:</Form.Label>
                  <Form.Check
                    type="checkbox"
                    label="Obrazy"
                    className="me-3"
                    defaultChecked
                  />
                  <Form.Check
                    type="checkbox"
                    label="GIF-y"
                    defaultChecked
                  />
                </Form.Group>
              </div>
            </Form>
          </Card.Body>
        </Card>
        
        <Row>
          {filteredGallery.map(item => (
            <Col md={4} className="mb-4" key={item.id}>
              <Card>
                {item.type === 'image' ? (
                  <Card.Img 
                    variant="top" 
                    src={item.url} 
                    alt={item.title} 
                    style={{ height: '200px', objectFit: 'cover' }}
                  />
                ) : (
                  <div style={{ height: '200px', overflow: 'hidden' }}>
                    <img 
                      src={item.url} 
                      alt={item.title} 
                      style={{ width: '100%', objectFit: 'cover' }}
                    />
                  </div>
                )}
                <Card.Body>
                  <Card.Title>{item.title}</Card.Title>
                  <Card.Text>
                    <small className="text-muted">
                      Autor: {item.creator}<br />
                      Data: {item.createdAt}<br />
                      Typ: {item.type === 'image' ? 'Obraz' : 'GIF'}
                    </small>
                  </Card.Text>
                  <div className="d-flex justify-content-between">
                    <Button variant="outline-primary" size="sm">
                      Pobierz
                    </Button>
                    <Button variant="outline-secondary" size="sm">
                      Szczegóły
                    </Button>
                  </div>
                </Card.Body>
              </Card>
            </Col>
          ))}
        </Row>
        
        {filteredGallery.length === 0 && (
          <div className="text-center py-5">
            <h4>Brak wyników dla "{filter}"</h4>
            <p>Spróbuj innych kryteriów wyszukiwania</p>
          </div>
        )}
      </Container>
    </>
  );
};

export default Gallery; 