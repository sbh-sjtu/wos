import React from 'react';
import { Layout, Typography } from 'antd';
import Header from '../header';
import Footer from '../footer';
import SearchInput from '../searchInput';
import '../../styles/advancedSearch.css';

const { Content } = Layout;
const { Title } = Typography;

const AdvancedSearch = () => {
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
        <div className="search-hero-section" style={{ backgroundColor: '#b82e28', paddingTop: '40px', paddingBottom: '80px' }}>
          <div className="container">
            <div className="advanced-search-container">
              <div className="search-input-wrapper">
                <SearchInput />
              </div>
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

export default AdvancedSearch;