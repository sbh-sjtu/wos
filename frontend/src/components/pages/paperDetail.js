import React from "react";
import { Layout, Button, Space } from "antd";
import { ArrowLeftOutlined, HomeOutlined } from '@ant-design/icons';
import Header from '../header';
import Footer from '../footer';
import { useLocation, useNavigate } from "react-router-dom";
import DetailCard from "../detailCard";

// 论文详情页面
function PaperDetail() {
    const location = useLocation();
    const navigate = useNavigate();
    // 获取传递过来的数据
    const paper = location.state?.paper;

    if (!paper) {
        // 如果没有数据，返回上一页或者展示一个错误提示
        return (
            <Layout className='paperDetail'>
                <Layout.Header
                    style={{
                        padding: 0,
                        backgroundColor: 'white',
                        boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
                        zIndex: 10,
                        position: 'sticky',
                        top: 0
                    }}
                >
                    <Header/>
                </Layout.Header>
                <Layout.Content style={{minHeight: 700, display: 'flex', alignItems: 'center', justifyContent: 'center'}}>
                    <div style={{ textAlign: 'center', padding: '50px' }}>
                        <h2>没有找到文献数据</h2>
                        <p style={{ marginBottom: '20px' }}>请返回搜索结果页面重新选择文献</p>
                        <Space>
                            <Button
                                type="primary"
                                icon={<ArrowLeftOutlined />}
                                onClick={() => navigate(-1)}
                                style={{ backgroundColor: '#b82e28' }}
                            >
                                返回上一页
                            </Button>
                            <Button
                                icon={<HomeOutlined />}
                                onClick={() => navigate('/')}
                            >
                                回到首页
                            </Button>
                        </Space>
                    </div>
                </Layout.Content>
                <Layout.Footer style={{
                    backgroundColor:'#b82e28',
                }}>
                    <Footer/>
                </Layout.Footer>
            </Layout>
        );
    }

    return (
        <Layout className='paperDetail'>
            <Layout.Header
                style={{
                    padding: 0,
                    backgroundColor: 'white',
                    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
                    zIndex: 10,
                    position: 'sticky',
                    top: 0
                }}
            >
                <Header/>
            </Layout.Header>

            <Layout.Content style={{minHeight: 700}}>
                {/* 面包屑导航 */}
                <div style={{
                    padding: '16px 20px',
                    backgroundColor: 'white',
                    borderBottom: '1px solid #f0f0f0',
                    position: 'sticky',
                    top: 64,
                    zIndex: 5
                }}>
                    <Space>
                        <Button
                            type="text"
                            icon={<ArrowLeftOutlined />}
                            onClick={() => navigate(-1)}
                            style={{ color: '#b82e28' }}
                        >
                            返回搜索结果
                        </Button>
                        <span style={{ color: '#666' }}>|</span>
                        <Button
                            type="text"
                            icon={<HomeOutlined />}
                            onClick={() => navigate('/')}
                            style={{ color: '#666' }}
                        >
                            首页
                        </Button>
                    </Space>
                </div>

                <div className='paperDetailContent'>
                    <DetailCard paperInfo={paper}/>
                </div>
            </Layout.Content>

            <Layout.Footer style={{
                backgroundColor:'#b82e28',
                padding: 0
            }}>
                <Footer/>
            </Layout.Footer>
        </Layout>
    );
}

export default PaperDetail;