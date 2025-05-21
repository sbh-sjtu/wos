import React from 'react';
import { Layout, Typography } from 'antd';
import Header from '../header';
import Footer from '../footer';
import FulltextSearch from '../fulltextSearch';
import '../../styles/generalSearch.css';

const { Content } = Layout;
const { Title } = Typography;

const GeneralSearch = () => {
  return (
    <Layout className="general-search-layout">
      <Layout.Header 
        className="general-search-header"
        style={{
          padding: 0,
          backgroundColor: 'white',
          boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
          zIndex: 10,
          position: 'sticky',
          top: 0
        }}
      >
        <Header />
      </Layout.Header>
      
      <Content className="general-search-content">
        <div className="search-hero-section">
          <div className="search-background"></div>
          <div className="search-overlay">
            <div className="container">
              <FulltextSearch />
            </div>
          </div>
        </div>
      </Content>
      
      <Layout.Footer
        style={{
          padding: 0,
          backgroundColor: '#b82e28',
        }}
      >
        <Footer />
      </Layout.Footer>
    </Layout>
  );
};

export default GeneralSearch;