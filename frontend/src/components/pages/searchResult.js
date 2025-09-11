import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, useSearchParams } from "react-router-dom";
import { Layout, Pagination, Button, Typography, Badge, Empty, Spin, Select, Input, Space, Divider, message, Tooltip, Modal, Alert, Progress } from "antd";
import { DownloadOutlined, SearchOutlined, PlusOutlined, DeleteOutlined, ClearOutlined, FileTextOutlined, DatabaseOutlined, CloseOutlined } from '@ant-design/icons';
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

    // 下载进度相关状态
    const [downloadProgress, setDownloadProgress] = useState({
        visible: false,
        taskId: null,
        status: 'idle',
        processedCount: 0,
        totalCount: 0,
        error: null,
        warning: null,
        downloadUrl: null,
        pollInterval: null
    });

    // 从 URL 参数获取当前页码，如果没有则默认为 1
    const pageFromUrl = parseInt(searchParams.get('page')) || 1;
    const [currentPage, setCurrentPage] = useState(pageFromUrl);

    const pageSize = 10;

    // 清理轮询器
    useEffect(() => {
        return () => {
            if (downloadProgress.pollInterval) {
                clearInterval(downloadProgress.pollInterval);
            }
        };
    }, [downloadProgress.pollInterval]);

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

    // 轮询下载进度
    const startPolling = (taskId) => {
        console.log('开始轮询进度:', taskId);

        const pollInterval = setInterval(async () => {
            try {
                const response = await axios.get(`http://localhost:8888/download/progress/${taskId}/status`);
                const progressData = response.data;

                console.log('轮询收到进度:', progressData);

                setDownloadProgress(prev => ({
                    ...prev,
                    ...progressData
                }));

                // 检查是否完成
                if (progressData.completed || progressData.status === 'completed') {
                    clearInterval(pollInterval);

                    if (progressData.downloadUrl) {
                        // 自动下载文件
                        const link = document.createElement('a');
                        link.href = `http://localhost:8888${progressData.downloadUrl}`;
                        link.setAttribute('download', progressData.fileName || 'wos_data.csv');
                        document.body.appendChild(link);
                        link.click();
                        link.parentNode.removeChild(link);

                        message.success('下载完成！');

                        // 延迟关闭进度窗口
                        setTimeout(() => {
                            setDownloadProgress(prev => ({
                                ...prev,
                                visible: false,
                                pollInterval: null
                            }));
                        }, 2000);
                    }
                } else if (progressData.status === 'error') {
                    clearInterval(pollInterval);
                    message.error(`下载失败: ${progressData.error}`);
                    setDownloadProgress(prev => ({
                        ...prev,
                        pollInterval: null
                    }));
                }

            } catch (error) {
                console.error('轮询进度失败:', error);
                clearInterval(pollInterval);
                setDownloadProgress(prev => ({
                    ...prev,
                    status: 'error',
                    error: '无法获取下载进度',
                    pollInterval: null
                }));
                message.error('无法获取下载进度');
            }
        }, 2000); // 每2秒轮询一次

        // 存储轮询器ID
        setDownloadProgress(prev => ({
            ...prev,
            pollInterval
        }));
    };

    // 添加搜索条件
    const handleAddFilter = () => {
        const newId = searchFilter.length + 1;
        setSearchFilter([...searchFilter, { id: newId, selects: ['AND', 1], input: '' }]);
    };

    // 删除搜索条件
    const handleDeleteFilter = (filterId) => {
        if (searchFilter.length === 1) return;
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
            setCurrentPage(1);
            setSearchParams({ page: '1' });

            if (newPaperInfo.length >= 200) {
                message.success(`搜索完成，当前显示前 200 条结果`);
            } else {
                message.success(`找到 ${newPaperInfo.length} 篇文献`);
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
        setSearchParams({ page: page.toString() });
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    // 点击标题跳转详情页
    const handleTitleClick = (paper) => {
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

        try {
            // 启动下载任务
            const response = await axios.post('http://localhost:8888/download/csv/all/start', searchFilter);
            const data = response.data;

            if (data.error) {
                message.error(data.error);
                return;
            }

            const { taskId } = data;

            // 显示进度界面
            setDownloadProgress({
                visible: true,
                taskId,
                status: 'started',
                processedCount: 0,
                totalCount: 0,
                error: null,
                warning: null,
                downloadUrl: null,
                pollInterval: null
            });

            // 关闭下载选项模态框
            setDownloadModalVisible(false);

            // 开始轮询进度
            startPolling(taskId);

        } catch (error) {
            console.error('启动下载失败:', error);
            message.error('启动下载失败，请稍后重试');
        }
    };

    // 取消下载
    const cancelDownload = async () => {
        // 停止轮询
        if (downloadProgress.pollInterval) {
            clearInterval(downloadProgress.pollInterval);
        }

        if (downloadProgress.taskId) {
            try {
                await axios.post(`http://localhost:8888/download/cancel/${downloadProgress.taskId}`);
                message.info('已取消下载');
            } catch (error) {
                console.error('取消下载失败:', error);
            }
        }

        setDownloadProgress({
            visible: false,
            taskId: null,
            status: 'idle',
            processedCount: 0,
            totalCount: 0,
            error: null,
            warning: null,
            downloadUrl: null,
            pollInterval: null
        });
    };

    // 获取进度百分比
    const getProgressPercent = () => {
        const total = downloadProgress.totalCount || 0;
        const processed = downloadProgress.processedCount || 0;
        if (total === 0) return 0;
        return Math.round((processed / total) * 100);
    };

    // 获取状态文本
    const getStatusText = () => {
        const processed = downloadProgress.processedCount || 0;
        const total = downloadProgress.totalCount || 0;

        switch (downloadProgress.status) {
            case 'started': return '正在启动...';
            case 'querying': return '正在查询数据...';
            case 'downloading': return '正在下载数据...';
            case 'processing': return `正在处理数据... (${processed.toLocaleString()}/${total.toLocaleString()})`;
            case 'generating_csv': return '正在生成CSV文件...';
            case 'completed': return '下载完成！';
            case 'error': return '下载失败';
            case 'cancelled': return '已取消';
            case 'no_data': return '没有找到数据';
            default: return '准备中...';
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
                                        <Text style={{ fontSize: '16px', marginRight: '10px' }}>当前显示文献</Text>
                                    </Badge>

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
                width={400}
            >
                <div style={{ padding: '20px 0' }}>
                    <Alert
                        message="下载说明"
                        description="系统最多支持下载 50,000 条记录"
                        type="info"
                        style={{ marginBottom: 24 }}
                    />

                    <div style={{ marginBottom: 16 }}>
                        <Button
                            type="primary"
                            icon={<FileTextOutlined />}
                            size="large"
                            loading={downloadLoading}
                            onClick={downloadCurrentData}
                            style={{
                                height: '60px',
                                backgroundColor: '#b82e28',
                                borderColor: '#b82e28',
                                width: '100%'
                            }}
                        >
                            <div style={{ textAlign: 'center' }}>
                                <div style={{ fontSize: '16px', fontWeight: 'bold' }}>
                                    下载当前数据
                                </div>
                                <div style={{ fontSize: '12px', opacity: 0.8 }}>
                                    下载当前显示的 {paperInfo.length} 条记录
                                </div>
                            </div>
                        </Button>
                    </div>

                    <Button
                        icon={<DatabaseOutlined />}
                        size="large"
                        onClick={downloadAllData}
                        style={{
                            height: '60px',
                            borderColor: '#b82e28',
                            color: '#b82e28',
                            width: '100%'
                        }}
                    >
                        <div style={{ textAlign: 'center' }}>
                            <div style={{ fontSize: '16px', fontWeight: 'bold' }}>
                                下载所有符合条件的数据
                            </div>
                            <div style={{ fontSize: '12px', opacity: 0.8 }}>
                                下载所有匹配搜索条件的记录（最多 50,000 条）
                            </div>
                        </div>
                    </Button>
                </div>
            </Modal>

            {/* 下载进度模态框 */}
            <Modal
                title="下载进度"
                visible={downloadProgress.visible}
                onCancel={cancelDownload}
                footer={[
                    <Button key="cancel" onClick={cancelDownload} disabled={downloadProgress.status === 'completed'}>
                        {downloadProgress.status === 'completed' ? '关闭' : '取消下载'}
                    </Button>
                ]}
                width={500}
                closable={false}
            >
                <div style={{ padding: '20px 0' }}>
                    <div style={{ marginBottom: 20 }}>
                        <Text strong style={{ fontSize: '16px' }}>
                            {getStatusText()}
                        </Text>
                    </div>

                    {/* 警告信息 */}
                    {downloadProgress.warning && (
                        <Alert
                            message="注意"
                            description={downloadProgress.warning}
                            type="warning"
                            style={{ marginBottom: 20 }}
                        />
                    )}

                    {downloadProgress.status === 'processing' && (
                        <div style={{ marginBottom: 20 }}>
                            <Progress
                                percent={getProgressPercent()}
                                status={downloadProgress.status === 'error' ? 'exception' : 'active'}
                                strokeColor={{
                                    '0%': '#b82e28',
                                    '100%': '#ff7875',
                                }}
                            />
                            <div style={{ textAlign: 'center', marginTop: 8 }}>
                                <Text type="secondary">
                                    已处理 {(downloadProgress.processedCount || 0).toLocaleString()} / {(downloadProgress.totalCount || 0).toLocaleString()} 条记录
                                </Text>
                            </div>
                        </div>
                    )}

                    {['started', 'querying', 'downloading', 'generating_csv'].includes(downloadProgress.status) && (
                        <div style={{ marginBottom: 20 }}>
                            <Progress
                                percent={
                                    downloadProgress.status === 'started' ? 10 :
                                        downloadProgress.status === 'querying' ? 30 :
                                            downloadProgress.status === 'downloading' ? 50 : 90
                                }
                                status="active"
                                strokeColor={{
                                    '0%': '#b82e28',
                                    '100%': '#ff7875',
                                }}
                            />
                        </div>
                    )}

                    {downloadProgress.status === 'error' && (
                        <Alert
                            message="下载失败"
                            description={downloadProgress.error}
                            type="error"
                            style={{ marginBottom: 20 }}
                        />
                    )}

                    {downloadProgress.status === 'completed' && (
                        <Alert
                            message="下载完成"
                            description="文件已自动开始下载，请检查浏览器下载文件夹"
                            type="success"
                            style={{ marginBottom: 20 }}
                        />
                    )}

                    <div style={{ textAlign: 'center' }}>
                        <Text type="secondary" style={{ fontSize: '12px' }}>
                            * 请勿关闭此窗口，直到下载完成
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