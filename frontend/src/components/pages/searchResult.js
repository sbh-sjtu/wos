import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, useSearchParams } from "react-router-dom";
import { Layout, Pagination, Button, Typography, Badge, Empty, Spin, Select, Input, Space, Divider, message, Tooltip, Modal, Alert } from "antd";
import { DownloadOutlined, SearchOutlined, PlusOutlined, DeleteOutlined, ClearOutlined, FileTextOutlined, DatabaseOutlined } from '@ant-design/icons';
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
    const [downloadModalVisible, setDownloadModalVisible] = useState(false);
    const [totalCount, setTotalCount] = useState(0); // 存储总记录数

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
            // 先获取总数
            const countResponse = await axios.post(
                "http://localhost:8888/main2022/advancedSearch/count",
                searchFilter
            );
            const count = countResponse.data.count || 0;
            setTotalCount(count);

            // 再获取前500条数据用于展示
            const response = await axios.post(
                "http://localhost:8888/main2022/advancedSearch",
                searchFilter
            );

            const newPaperInfo = response.data;
            setPaperInfo(newPaperInfo);
            setCurrentPage(1); // 重置到第一页
            setSearchParams({ page: '1' }); // 同时更新URL

            if (count > 500) {
                message.success(`找到 ${count} 篇文献，当前显示前 500 条`);
            } else {
                message.success(`找到 ${count} 篇文献`);
            }
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

    // 显示下载选项模态框
    const showDownloadModal = () => {
        if (paperInfo.length === 0) {
            message.warning('没有数据可以下载');
            return;
        }
        setDownloadModalVisible(true);
    };

    // 下载当前展示的数据（最多500条）
    const downloadCurrentData = async () => {
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
            link.setAttribute('download', 'wos_current_data.csv');
            document.body.appendChild(link);
            link.click();
            link.parentNode.removeChild(link);

            message.success(`已下载当前 ${paperInfo.length} 条数据`);
            setDownloadModalVisible(false);
        } catch (error) {
            console.error('文件下载失败:', error);
            message.error('下载失败，请稍后重试');
        } finally {
            setDownloadLoading(false);
        }
    };

    // 下载所有符合条件的数据
    const downloadAllData = async () => {
        if (searchFilter.length === 0 || searchFilter.every(f => !f.input.trim())) {
            message.warning('请先执行搜索操作');
            return;
        }

        setDownloadLoading(true);
        try {
            const response = await axios.post('http://localhost:8888/download/csv/all', searchFilter, {
                responseType: 'blob',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', 'wos_all_data.csv');
            document.body.appendChild(link);
            link.click();
            link.parentNode.removeChild(link);

            message.success(`已下载所有 ${totalCount} 条数据`);
            setDownloadModalVisible(false);
        } catch (error) {
            console.error('文件下载失败:', error);
            if (error.response?.status === 413) {
                message.error('数据量过大，请缩小搜索范围后重试');
            } else {
                message.error('下载失败，请稍后重试');
            }
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

                                    {totalCount > 0 && totalCount !== paperInfo.length ? (
                                        <div style={{ marginBottom: 16 }}>
                                            <Badge
                                                count={totalCount}
                                                style={{ backgroundColor: '#b82e28' }}
                                                overflowCount={999999}
                                            >
                                                <Text style={{ fontSize: '16px', marginRight: '10px' }}>总计文献</Text>
                                            </Badge>
                                            <div style={{ marginTop: 8 }}>
                                                <Badge
                                                    count={paperInfo.length}
                                                    style={{ backgroundColor: '#52c41a' }}
                                                    overflowCount={9999}
                                                >
                                                    <Text style={{ fontSize: '14px', marginRight: '10px' }}>当前显示</Text>
                                                </Badge>
                                            </div>
                                        </div>
                                    ) : (
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
                                    )}

                                    <div className="action-buttons">
                                        <Button
                                            type="primary"
                                            icon={<DownloadOutlined />}
                                            onClick={showDownloadModal}
                                            disabled={paperInfo.length === 0}
                                            style={{
                                                backgroundColor: '#b82e28',
                                                borderColor: '#b82e28',
                                                width: '100%',
                                                marginBottom: 8
                                            }}
                                        >
                                            导出数据
                                        </Button>

                                        <div className="result-stats">
                                            <Text type="secondary" style={{ fontSize: '12px' }}>
                                                当前显示 {indexOfFirstPaper + 1}-{Math.min(indexOfLastPaper, paperInfo.length)} 条，
                                                共 {paperInfo.length} 条记录
                                                {totalCount > paperInfo.length && (
                                                    <div style={{ marginTop: 4 }}>
                                                        总计 {totalCount} 条匹配记录
                                                    </div>
                                                )}
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

            {/* 下载选项模态框 */}
            <Modal
                title="选择下载选项"
                visible={downloadModalVisible}
                onCancel={() => setDownloadModalVisible(false)}
                footer={null}
                width={500}
            >
                <div style={{ padding: '20px 0' }}>
                    <Alert
                        message="下载提示"
                        description={`当前搜索结果${totalCount > paperInfo.length ? `共找到 ${totalCount} 条记录，页面显示前 ${paperInfo.length} 条` : `共 ${paperInfo.length} 条记录`}`}
                        type="info"
                        style={{ marginBottom: 24 }}
                    />

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                        <Button
                            type="primary"
                            icon={<FileTextOutlined />}
                            size="large"
                            loading={downloadLoading}
                            onClick={downloadCurrentData}
                            style={{
                                height: '60px',
                                backgroundColor: '#52c41a',
                                borderColor: '#52c41a'
                            }}
                        >
                            <div style={{ textAlign: 'left' }}>
                                <div style={{ fontSize: '16px', fontWeight: 'bold' }}>
                                    下载当前数据
                                </div>
                                <div style={{ fontSize: '12px', opacity: 0.8 }}>
                                    下载页面展示的 {paperInfo.length} 条记录
                                </div>
                            </div>
                        </Button>

                        {totalCount > paperInfo.length && (
                            <Button
                                type="primary"
                                icon={<DatabaseOutlined />}
                                size="large"
                                loading={downloadLoading}
                                onClick={downloadAllData}
                                style={{
                                    height: '60px',
                                    backgroundColor: '#b82e28',
                                    borderColor: '#b82e28'
                                }}
                            >
                                <div style={{ textAlign: 'left' }}>
                                    <div style={{ fontSize: '16px', fontWeight: 'bold' }}>
                                        下载全部数据
                                    </div>
                                    <div style={{ fontSize: '12px', opacity: 0.8 }}>
                                        下载所有 {totalCount} 条匹配记录（可能需要较长时间）
                                    </div>
                                </div>
                            </Button>
                        )}
                    </div>

                    <div style={{ marginTop: 16, textAlign: 'center' }}>
                        <Text type="secondary" style={{ fontSize: '12px' }}>
                            * 大量数据下载可能需要较长时间，请耐心等待
                        </Text>
                    </div>
                </div>
            </Modal>

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