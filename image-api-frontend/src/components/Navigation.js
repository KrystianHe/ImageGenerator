import React from 'react';
import { Navbar, Nav, Container } from 'react-bootstrap';
import { Link } from 'react-router-dom';

const Navigation = () => {
  return (
    <Navbar bg="dark" variant="dark" expand="lg">
      <Container>
        <Navbar.Brand as={Link} to="/">Generator Obrazów AI</Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="me-auto">
            <Nav.Link as={Link} to="/text-analyzer">Analiza Tekstu</Nav.Link>
            <Nav.Link as={Link} to="/image-generator">Generator Obrazów</Nav.Link>
            <Nav.Link as={Link} to="/gif-generator">Generator GIF</Nav.Link>
            <Nav.Link as={Link} to="/jokes">Żarty</Nav.Link>
            <Nav.Link as={Link} to="/gallery">Galeria</Nav.Link>
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default Navigation; 