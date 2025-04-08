import React, { useState, useEffect } from 'react';
import { Container, Card, Button, Spinner, Row, Col, Form } from 'react-bootstrap';
import Navigation from '../components/Navigation';

const Jokes = () => {
  const [joke, setJoke] = useState(null);
  const [loading, setLoading] = useState(false);
  const [category, setCategory] = useState('random');
  const [customTopic, setCustomTopic] = useState('');
  const [isCustomTopic, setIsCustomTopic] = useState(false);
  
  const jokeCategories = [
    { id: 'custom', name: 'Własny temat', icon: 'bi-pencil' },
    { id: 'random', name: 'Losowe', icon: 'bi-shuffle' }
  ];
  
  const generateCustomJoke = (topic) => {
    const customJokeTemplates = [
      {
        setup: `Co ${topic} robi na dachu?`,
        punchline: "Dach daje!"
      },
      {
        setup: `Co mówi ${topic} do drugiego ${topic}?`,
        punchline: "Czy my się znamy?"
      },
      {
        setup: `Jak nazywa się ${topic} w kosmosie?`,
        punchline: `Kosmo-${topic}!`
      },
      {
        setup: `Co robi ${topic} gdy jest głodny?`,
        punchline: `Idzie do ${topic}-restauracji!`
      },
      {
        setup: `Dlaczego ${topic} nie umie kłamać?`,
        punchline: `Bo zawsze mówi ${topic}-ską prawdę!`
      },
      {
        setup: `Co by było gdyby ${topic} został prezydentem?`,
        punchline: `Mielibyśmy ${topic}-krację!`
      },
      {
        setup: `Co ${topic} robi wieczorem?`,
        punchline: `${topic}-suje się do snu!`
      },
      {
        setup: `Jaki jest ulubiony sport ${topic}?`,
        punchline: `${topic}-ball!`
      },
      {
        setup: `Co najbardziej lubi jeść ${topic}?`,
        punchline: `${topic}-czki!`
      },
      {
        setup: `Jakie buty nosi ${topic}?`,
        punchline: `${topic}-kasy!`
      }
    ];
    
    const randomIndex = Math.floor(Math.random() * customJokeTemplates.length);
    return customJokeTemplates[randomIndex];
  };

  const generateRandomJoke = () => {
    const randomJokes = [
      {
        setup: "Dlaczego książki nie lubią drzemek?",
        punchline: "Bo boją się, że stracą wątek."
      },
      {
        setup: "Co robi programista na siłowni?",
        punchline: "Podnosi swoje umiejętności."
      },
      {
        setup: "Ile programistów potrzeba do wymiany żarówki?",
        punchline: "Żadnego, to problem sprzętowy."
      },
      {
        setup: "Co mówi informatyk, gdy dostaje prezent?",
        punchline: "Dzięki za support!"
      },
      {
        setup: "Jak nazywa się telefon matematyka?",
        punchline: "Komórka."
      }
    ];
    
    const randomIndex = Math.floor(Math.random() * randomJokes.length);
    return randomJokes[randomIndex];
  };
  
  const fetchJoke = () => {
    setLoading(true);
    
    // Symulacja pobierania danych
    setTimeout(() => {
      let selectedJoke;
      
      if (category === 'custom' && customTopic.trim()) {
        selectedJoke = generateCustomJoke(customTopic.trim());
      } else {
        selectedJoke = generateRandomJoke();
      }
      
      setJoke(selectedJoke);
      setLoading(false);
    }, 800);
  };
  
  useEffect(() => {
    if (category === 'custom') {
      setIsCustomTopic(true);
    } else {
      setIsCustomTopic(false);
      fetchJoke();
    }
  }, [category]);
  
  const handleSubmitCustomTopic = (e) => {
    e.preventDefault();
    if (customTopic.trim()) {
      fetchJoke();
    }
  };
  
  return (
    <>
      <Navigation />
      <Container className="py-5">
        <div className="text-center mb-5">
          <h1 className="mb-3">Generator Żartów</h1>
          <p className="lead">Potrzebujesz się uśmiechnąć? Wybierz kategorię lub wpisz własny temat!</p>
        </div>
        
        <Row className="justify-content-center mb-5">
          <Col lg={8}>
            <div className="category-selection">
              <h4 className="text-center mb-3">Wybierz kategorię</h4>
              <Row className="g-3">
                {jokeCategories.map(cat => (
                  <Col xs={6} md={6} key={cat.id}>
                    <div 
                      className={`style-card ${category === cat.id ? 'selected' : ''}`}
                      onClick={() => setCategory(cat.id)}
                    >
                      <i className={`bi ${cat.icon}`}></i>
                      <span>{cat.name}</span>
                    </div>
                  </Col>
                ))}
              </Row>
            </div>
            
            {isCustomTopic && (
              <Form className="custom-topic-form mt-4 animate-fadeIn" onSubmit={handleSubmitCustomTopic}>
                <Form.Group>
                  <Form.Label>Wpisz temat żartu</Form.Label>
                  <div className="d-flex">
                    <Form.Control
                      type="text"
                      placeholder="np. kot, programowanie, pizza..."
                      value={customTopic}
                      onChange={(e) => setCustomTopic(e.target.value)}
                      className="me-2"
                    />
                    <Button variant="primary" type="submit">
                      Generuj
                    </Button>
                  </div>
                </Form.Group>
              </Form>
            )}
          </Col>
        </Row>
        
        <Row className="justify-content-center">
          <Col lg={8}>
            <Card className="shadow-lg border-0 joke-card">
              <Card.Body className="p-5">
                {loading ? (
                  <div className="text-center p-5">
                    <Spinner animation="border" variant="primary" />
                    <p className="mt-3">Szukam zabawnego żartu...</p>
                  </div>
                ) : joke ? (
                  <div className="joke-content animate-fadeIn">
                    <h3 className="joke-setup mb-4">{joke.setup}</h3>
                    <div className="joke-divider my-4"></div>
                    <h2 className="joke-punchline mb-4">{joke.punchline}</h2>
                    
                    <div className="text-center mt-5">
                      <Button 
                        variant="primary"
                        size="lg"
                        className="px-5 py-3"
                        onClick={fetchJoke}
                      >
                        <i className="bi bi-arrow-repeat me-2"></i>
                        Następny żart
                      </Button>
                    </div>
                  </div>
                ) : (
                  <div className="text-center p-5">
                    <p>Nie udało się załadować żartu.</p>
                  </div>
                )}
              </Card.Body>
            </Card>
            
            <div className="text-center mt-4">
              <p>
                <i className="bi bi-lightbulb me-2"></i>
                {isCustomTopic ? 
                  "Wskazówka: Wpisz dowolny temat, a wygenerujemy żart na jego podstawie!" :
                  "Wskazówka: Żarty są generowane losowo z naszej bazy danych."}
              </p>
            </div>
          </Col>
        </Row>
      </Container>
    </>
  );
};

export default Jokes; 