import React from 'react';
import { Layout } from 'antd';
import Header from '../header';
import Footer from '../footer';
import SearchInput from '../searchInput';
import '../../styles/advancedSearch.css';

const { Content } = Layout;

const AdvancedSearch = () => {
    return (
        <Layout className="advanced-search-layout">
            <Layout.Header
                className="advanced-search-header"
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

            <Content className="advanced-search-content">
                <div className="search-hero-section">
                    <div className="container">
                        <SearchInput />
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