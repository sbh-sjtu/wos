import React, { useState } from 'react';
import { useNavigate, useLocation } from "react-router-dom";
import { Layout, Pagination, Button, Typography, Badge, Empty, Spin } from "antd";
import { DownloadOutlined } from '@ant-design/icons';
import Header from '../header';
import Footer from '../footer';
import PaperCard from '../paperCard';
import axios from 'axios';
import '../../styles/searchResult.css';
import SearchInput from '../searchInput'; // 引入 SearchInput 组件

const { Content, Sider } = Layout;
const { Title, Text } = Typography;

function SearchResult() {
    const { state } = useLocation();
    const navigate = useNavigate();
    // 获取 papers 数据，如果没有则为 []
    const [paperInfo, setPaperInfo] = useState(state?.paperInfo || []);
    const [loading, setLoading] = useState(false);
    const [currentPage, setCurrentPage] = useState(1);
    const pageSize = 10; // 每页显示的数量

    // 计算当前页显示的数据
    const indexOfLastPaper = currentPage * pageSize;
    const indexOfFirstPaper = indexOfLastPaper - pageSize;
    const currentPapers = paperInfo.slice(indexOfFirstPaper, indexOfLastPaper);

    // 改变页码
    const onPageChange = (page) => {
        setCurrentPage(page);
        // 滚动到页面顶部
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    const handleTitleClick = (paper) => {
        navigate("/detail", { state: { paper } }); // 传递 paperInfo 对象
    };

    const downLoadCSV = async () => {
        if (paperInfo.length === 0) return;

        setLoading(true);
        try {
            // 发送下载请求
            const response = await axios.post('http://localhost:8888/download/csv', paperInfo, {
                responseType: 'blob', // 指定响应类型为 Blob，用于处理文件流
                headers: {
                    'Content-Type': 'application/json', // 指定请求体为 JSON 格式
                },
            });

            // 创建一个下载链接并触发下载
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', 'wos_paper_detail.csv'); // 设置下载文件名
            document.body.appendChild(link);
            link.click();
            link.parentNode.removeChild(link); // 下载完成后移除链接
        } catch (error) {
            console.error('文件下载失败:', error);
        } finally {
            setLoading(false);
        }
    };

    // 处理高级搜索请求
    const handleAdvancedSearch = async (filters) => {
        setLoading(true);
        try {
            const response = await axios.post('http://localhost:8888/main2022/advancedSearch', filters);
            setPaperInfo(response.data);
            setCurrentPage(1); // 重置页码
        } catch (error) {
            console.error('高级搜索失败:', error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <Layout className="search-result-layout">
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
                <Header />
            </Layout.Header>

            <Layout className="result-content">
                <div className="result-container">
                    <Layout hasSider>
                        <Sider
                            width={580}
                            className="result-sider"
                            theme="light"
                        >
                            <div className="sider-content">
                                <div className="result-summary">
                                    <Title level={4}>搜索结果</Title>
                                    <Badge
                                        count={paperInfo.length}
                                        style={{
                                            backgroundColor: '#b82e28',
                                            marginBottom: '20px'
                                        }}
                                        overflowCount={9999}
                                    >
                                        <Text style={{ fontSize: '16px', marginRight: '10px' }}>找到文献</Text>
                                    </Badge>

                                    <div className="action-buttons">
                                        <Button
                                            type="primary"
                                            icon={<DownloadOutlined />}
                                            onClick={downLoadCSV}
                                            loading={loading}
                                            disabled={paperInfo.length === 0}
                                            style={{
                                                backgroundColor: '#b82e28',
                                                borderColor: '#b82e28',
                                                width: '100%'
                                            }}
                                        >
                                            导出为CSV
                                        </Button>
                                    </div>
                                </div>

                                <div className="filter-options">
                                    <Title level={5}>筛选选项</Title>
                                    <SearchInput onSearch={handleAdvancedSearch} /> {/* 替换为高级搜索组件 */}
                                </div>
                            </div>
                        </Sider>

                        <Content className="result-main-content">
                            <Spin spinning={loading}>
                                {/* 顶部分页器 */}
                                {paperInfo.length > 0 && (
                                    <div className="pagination-container-top" style={{ marginBottom: '16px' }}>
                                        <Pagination
                                            current={currentPage}
                                            pageSize={pageSize}
                                            total={paperInfo.length}
                                            onChange={onPageChange}
                                            showTotal={total => `共显示 ${total} 条记录`}
                                            showQuickJumper
                                            showSizeChanger={false}
                                        />
                                    </div>
                                )}

                                {paperInfo.length > 0 ? (
                                    <div className="paper-cards-container">
                                        {currentPapers.map((paper, index) => (
                                            <div key={index} className="paper-card-wrapper">
                                                <PaperCard
                                                    paperInfo={paper}
                                                    onTitleClick={() => handleTitleClick(paper)}
                                                />
                                            </div>
                                        ))}
                                    </div>
                                ) : (
                                    <div className="no-results">
                                        <Empty
                                            description="没有找到匹配的文献"
                                            image={Empty.PRESENTED_IMAGE_SIMPLE}
                                        />
                                    </div>
                                )}

                                {paperInfo.length > 0 && (
                                    <div className="pagination-container-bottom">
                                        <Pagination
                                            current={currentPage}
                                            pageSize={pageSize}
                                            total={paperInfo.length}
                                            onChange={onPageChange}
                                            showTotal={total => `共显示 ${total} 条记录`}
                                            showQuickJumper
                                            showSizeChanger={false}
                                        />
                                    </div>
                                )}
                            </Spin>
                        </Content>
                    </Layout>
                </div>
            </Layout>

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
}

export default SearchResult;