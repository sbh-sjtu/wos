import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, useSearchParams } from "react-router-dom";
import { Layout, Pagination, Button, Typography, Badge, Empty, Spin, Select, Input, Space, Divider, message, Tooltip } from "antd";
import { DownloadOutlined, SearchOutlined, PlusOutlined, DeleteOutlined, ClearOutlined } from '@ant-design/icons';
import Header from '../header';
import Footer from '../footer';
import PaperCard from '../paperCard';
import axios from 'axios';
import '../../styles/searchResult.css';

const { Content, Sider } = Layout;
const { Title, Text } = Typography;
const { Option } = Select;

function SearchResult() {
    const { state } = useLocation();
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();

    // 获取传递的数据
    const initialPaperInfo = state?.paperInfo || [];
    const initialSearchFilter = state?.searchFilter || [{ id: 1, selects: ['AND', 1], input: '' }];

    const [paperInfo, setPaperInfo] = useState(initialPaperInfo);
    const [searchFilter, setSearchFilter] = useState(initialSearchFilter);
    const [loading, setLoading] = useState(false);
    const [downloadLoading, setDownloadLoading] = useState(false);

    // 从 URL 参数获取当前页码，如果没有则默认为 1
    const pageFromUrl = parseInt(searchParams.get('page')) || 1;
    const [currentPage, setCurrentPage] = useState(pageFromUrl);

    const pageSize = 10;

    // 将搜索数据保存到 sessionStorage
    useEffect(() => {
        if (paperInfo.length > 0) {
            sessionStorage.setItem('searchResults', JSON.stringify(paperInfo));
            sessionStorage.setItem('searchFilters', JSON.stringify(searchFilter));
        }
    }, [paperInfo, searchFilter]);

    // 如果没有数据但有 sessionStorage 数据，则恢复数据
    useEffect(() => {
        if (paperInfo.length === 0) {
            const savedResults = sessionStorage.getItem('searchResults');
            const savedFilters = sessionStorage.getItem('searchFilters');

            if (savedResults && savedFilters) {
                try {
                    const parsedResults = JSON.parse(savedResults);
                    const parsedFilters = JSON.parse(savedFilters);

                    if (parsedResults.length > 0) {
                        setPaperInfo(parsedResults);
                        setSearchFilter(parsedFilters);
                    }
                } catch (error) {
                    console.error('恢复搜索数据失败:', error);
                }
            }
        }
    }, [paperInfo.length]);

    // 当组件挂载时，同步 URL 中的页码
    useEffect(() => {
        const urlPage = parseInt(searchParams.get('page')) || 1;
        if (urlPage !== currentPage) {
            setCurrentPage(urlPage);
        }
    }, [searchParams, currentPage]);

    // 计算当前页显示的数据
    const indexOfLastPaper = currentPage * pageSize;
    const indexOfFirstPaper = indexOfLastPaper - pageSize;
    const currentPapers = paperInfo.slice(indexOfFirstPaper, indexOfLastPaper);

    // 添加搜索条件
    const handleAddFilter = () => {
        const newId = searchFilter.length + 1;
        setSearchFilter([...searchFilter, { id: newId, selects: ['AND', 1], input: '' }]);
    };

    // 删除搜索条件
    const handleDeleteFilter = (filterId) => {
        if (searchFilter.length === 1) return; // 保留至少一个条件
        const updatedFilters = searchFilter.filter(filter => filter.id !== filterId);
        const reassignedFilters = updatedFilters.map((filter, index) => (
            { ...filter, id: index + 1 }
        ));
        setSearchFilter(reassignedFilters);
    };

    // 清空所有条件
    const handleClearAll = () => {
        setSearchFilter([{ id: 1, selects: ['AND', 1], input: '' }]);
    };

    // 更新选择值
    const handleSelectChange = (filterId, selectIndex, newValue) => {
        setSearchFilter(searchFilter.map(filter =>
            filter.id === filterId
                ? { ...filter, selects: filter.selects.map((value, index) =>
                        index === selectIndex ? newValue : value) }
                : filter
        ));
    };

    // 更新输入值
    const handleInputChange = (filterId, newValue) => {
        setSearchFilter(searchFilter.map(filter =>
            filter.id === filterId
                ? { ...filter, input: newValue }
                : filter
        ));
    };

    // 执行搜索
    const handleSearch = async () => {
        const emptyFields = searchFilter.filter(filter => !filter.input.trim());
        if (emptyFields.length > 0) {
            message.warning("请完整填写搜索条件");
            return;
        }

        setLoading(true);

        try {
            const response = await axios.post(
                "http://localhost:8888/main2022/advancedSearch",
                searchFilter
            );

            const newPaperInfo = response.data;
            setPaperInfo(newPaperInfo);
            setCurrentPage(1); // 重置到第一页
            setSearchParams({ page: '1' }); // 同时更新URL
            message.success(`找到 ${newPaperInfo.length} 篇文献`);
        } catch (error) {
            console.error("搜索请求失败:", error);
            message.error("搜索失败，请稍后重试");
        } finally {
            setLoading(false);
        }
    };

    // 页码变化
    const onPageChange = (page) => {
        setCurrentPage(page);
        // 更新 URL 参数，保存当前页码
        setSearchParams({ page: page.toString() });
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    // 点击标题跳转详情页
    const handleTitleClick = (paper) => {
        // 跳转前确保当前页码已经在 URL 中
        if (!searchParams.get('page')) {
            setSearchParams({ page: currentPage.toString() });
        }
        navigate("/detail", {
            state: { paper }
        });
    };

    // 下载CSV
    const downloadCSV = async () => {
        if (paperInfo.length === 0) return;

        setDownloadLoading(true);
        try {
            const response = await axios.post('http://localhost:8888/download/csv', paperInfo, {
                responseType: 'blob',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', 'wos_paper_detail.csv');
            document.body.appendChild(link);
            link.click();
            link.parentNode.removeChild(link);
        } catch (error) {
            console.error('文件下载失败:', error);
            message.error('下载失败，请稍后重试');
        } finally {
            setDownloadLoading(false);
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
                            width={320}
                            className="result-sider"
                            theme="light"
                        >
                            <div className="sider-content">
                                {/* 搜索结果统计 */}
                                <div className="result-summary">
                                    <Title level={4} style={{ marginBottom: 16 }}>搜索结果</Title>
                                    <Badge
                                        count={paperInfo.length}
                                        style={{
                                            backgroundColor: '#b82e28',
                                            marginBottom: 16
                                        }}
                                        overflowCount={9999}
                                    >
                                        <Text style={{ fontSize: '16px', marginRight: '10px' }}>找到文献</Text>
                                    </Badge>

                                    <div className="action-buttons">
                                        <Button
                                            type="primary"
                                            icon={<DownloadOutlined />}
                                            onClick={downloadCSV}
                                            loading={downloadLoading}
                                            disabled={paperInfo.length === 0}
                                            style={{
                                                backgroundColor: '#b82e28',
                                                borderColor: '#b82e28',
                                                width: '100%',
                                                marginBottom: 8
                                            }}
                                        >
                                            导出为CSV
                                        </Button>

                                        <div className="result-stats">
                                            <Text type="secondary" style={{ fontSize: '12px' }}>
                                                当前显示 {indexOfFirstPaper + 1}-{Math.min(indexOfLastPaper, paperInfo.length)} 条，
                                                共 {paperInfo.length} 条记录
                                            </Text>
                                        </div>
                                    </div>
                                </div>

                                <Divider style={{ margin: '16px 0' }} />

                                {/* 精简版高级搜索 */}
                                <div className="advanced-search-panel">
                                    <Title level={5} style={{ marginBottom: 16, color: '#b82e28' }}>
                                        修改搜索条件
                                    </Title>

                                    <Spin spinning={loading}>
                                        <div className="compact-search-form">
                                            {/* 第一个条件（不显示AND/OR选择器） */}
                                            <div className="compact-condition">
                                                <Select
                                                    style={{ width: '100%', marginBottom: 8 }}
                                                    value={searchFilter[0]?.selects[1]}
                                                    onChange={(value) => handleSelectChange(1, 1, value)}
                                                    size="small"
                                                >
                                                    <Option value={1}>Topic</Option>
                                                    <Option value={2}>Title</Option>
                                                    <Option value={3}>Author</Option>
                                                    <Option value={4}>Publication/Source Titles</Option>
                                                    <Option value={5}>Year Published</Option>
                                                    <Option value={6}>DOI</Option>
                                                </Select>
                                                <Input
                                                    placeholder="输入关键词..."
                                                    value={searchFilter[0]?.input || ''}
                                                    onChange={(e) => handleInputChange(1, e.target.value)}
                                                    size="small"
                                                />
                                            </div>

                                            {/* 其他条件 */}
                                            {searchFilter.slice(1).map(filter => (
                                                <div key={filter.id} className="compact-condition">
                                                    <div style={{ display: 'flex', alignItems: 'center', marginBottom: 8 }}>
                                                        <Select
                                                            style={{ width: 70, marginRight: 8 }}
                                                            value={filter.selects[0]}
                                                            onChange={(value) => handleSelectChange(filter.id, 0, value)}
                                                            size="small"
                                                        >
                                                            <Option value="AND">AND</Option>
                                                            <Option value="OR">OR</Option>
                                                        </Select>
                                                        <Select
                                                            style={{ flex: 1 }}
                                                            value={filter.selects[1]}
                                                            onChange={(value) => handleSelectChange(filter.id, 1, value)}
                                                            size="small"
                                                        >
                                                            <Option value={1}>Topic</Option>
                                                            <Option value={2}>Title</Option>
                                                            <Option value={3}>Author</Option>
                                                            <Option value={4}>Publication/Source Titles</Option>
                                                            <Option value={5}>Year Published</Option>
                                                            <Option value={6}>DOI</Option>
                                                        </Select>
                                                        <Tooltip title="删除条件">
                                                            <Button
                                                                type="text"
                                                                icon={<DeleteOutlined />}
                                                                onClick={() => handleDeleteFilter(filter.id)}
                                                                size="small"
                                                                style={{ marginLeft: 4, color: '#b82e28' }}
                                                            />
                                                        </Tooltip>
                                                    </div>
                                                    <Input
                                                        placeholder="输入关键词..."
                                                        value={filter.input}
                                                        onChange={(e) => handleInputChange(filter.id, e.target.value)}
                                                        size="small"
                                                    />
                                                </div>
                                            ))}

                                            {/* 操作按钮 */}
                                            <div className="compact-actions">
                                                <Button
                                                    type="dashed"
                                                    icon={<PlusOutlined />}
                                                    onClick={handleAddFilter}
                                                    size="small"
                                                    style={{
                                                        borderColor: '#b82e28',
                                                        color: '#b82e28',
                                                        marginBottom: 8,
                                                        width: '100%'
                                                    }}
                                                >
                                                    添加条件
                                                </Button>

                                                <Space size="small" style={{ width: '100%' }}>
                                                    <Button
                                                        icon={<ClearOutlined />}
                                                        onClick={handleClearAll}
                                                        size="small"
                                                        style={{ flex: 1 }}
                                                    >
                                                        清空
                                                    </Button>
                                                    <Button
                                                        type="primary"
                                                        icon={<SearchOutlined />}
                                                        onClick={handleSearch}
                                                        size="small"
                                                        style={{
                                                            background: '#b82e28',
                                                            flex: 1
                                                        }}
                                                    >
                                                        搜索
                                                    </Button>
                                                </Space>
                                            </div>
                                        </div>
                                    </Spin>
                                </div>
                            </div>
                        </Sider>

                        <Content className="result-main-content">
                            <Spin spinning={loading}>
                                {paperInfo.length > 0 && (
                                    <div className="pagination-container_top">
                                        <Pagination
                                            current={currentPage}
                                            pageSize={pageSize}
                                            total={paperInfo.length}
                                            onChange={onPageChange}
                                            showTotal={total => `共 ${total} 条记录`}
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
                                    <div className="pagination-container_bottom">
                                        <Pagination
                                            current={currentPage}
                                            pageSize={pageSize}
                                            total={paperInfo.length}
                                            onChange={onPageChange}
                                            showTotal={total => `共 ${total} 条记录`}
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