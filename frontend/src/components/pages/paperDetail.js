import React, { useState, useEffect } from "react";
import { Layout, Button, Space, Spin, Alert, message, Tooltip } from "antd";
import { ArrowLeftOutlined, HomeOutlined, LoadingOutlined, ReloadOutlined, LinkOutlined, CopyOutlined } from '@ant-design/icons';
import Header from '../header';
import Footer from '../footer';
import { useLocation, useNavigate, useParams } from "react-router-dom";
import DetailCard from "../detailCard";
import axios from 'axios';

// 论文详情页面 - 支持URL路由
function PaperDetail() {
    const location = useLocation();
    const navigate = useNavigate();
    const { wosUid } = useParams(); // 获取URL参数中的wosUid

    // 状态管理
    const [paper, setPaper] = useState(location.state?.paper || null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [queryTime, setQueryTime] = useState(null);

    // 从URL获取文献数据
    const fetchPaperDetail = async (wosUidParam) => {
        setLoading(true);
        setError(null);

        try {
            // 确保wosUid已经编码（处理特殊字符）
            const encodedWosUid = encodeURIComponent(wosUidParam);
            const response = await axios.get(
                `http://localhost:8888/main2022/detail/${encodedWosUid}`
            );

            if (response.data.success && response.data.data) {
                setPaper(response.data.data);
                setQueryTime(response.data.queryTime);

                // 可选：缓存到sessionStorage
                sessionStorage.setItem(
                    `paper_${wosUidParam}`,
                    JSON.stringify(response.data.data)
                );

                message.success(`文献加载成功 (${response.data.queryTime})`);
            } else {
                setError(response.data.message || '未找到该文献');
                message.error(response.data.message || '未找到该文献');
            }
        } catch (err) {
            console.error('获取文献详情失败:', err);
            const errorMsg = err.response?.data?.message ||
                err.response?.data?.error ||
                '加载文献失败，请稍后重试';
            setError(errorMsg);
            message.error(errorMsg);
        } finally {
            setLoading(false);
        }
    };

    // 检查缓存
    const checkCache = (wosUidParam) => {
        const cached = sessionStorage.getItem(`paper_${wosUidParam}`);
        if (cached) {
            try {
                const cachedPaper = JSON.parse(cached);
                console.log('使用缓存的文献数据');
                return cachedPaper;
            } catch (e) {
                console.error('解析缓存数据失败:', e);
                sessionStorage.removeItem(`paper_${wosUidParam}`);
            }
        }
        return null;
    };

    // 组件挂载或URL参数变化时执行
    useEffect(() => {
        // 如果URL中有wosUid参数
        if (wosUid) {
            const decodedWosUid = decodeURIComponent(wosUid);

            // 如果没有传递paper数据，或者paper的wos_uid与URL不匹配
            if (!paper || paper.wos_uid !== decodedWosUid) {
                // 先检查缓存
                const cachedPaper = checkCache(decodedWosUid);
                if (cachedPaper) {
                    setPaper(cachedPaper);
                } else {
                    // 从后端获取数据
                    fetchPaperDetail(decodedWosUid);
                }
            }
        } else if (!paper) {
            // 如果既没有URL参数也没有传递的数据，显示错误
            setError('没有找到文献数据');
        }
    }, [wosUid]); // 依赖wosUid参数

    // 返回上一页
    const handleGoBack = () => {
        // 检查是否从搜索页面来
        if (location.state?.fromSearch) {
            navigate(-1);
        } else {
            // 否则返回搜索结果页
            navigate('/searchResult');
        }
    };

    // 重新加载
    const handleReload = () => {
        if (wosUid) {
            const decodedWosUid = decodeURIComponent(wosUid);
            // 清除缓存
            sessionStorage.removeItem(`paper_${decodedWosUid}`);
            // 重新获取
            fetchPaperDetail(decodedWosUid);
        }
    };

    // 复制链接
    const handleCopyLink = () => {
        const url = window.location.href;
        navigator.clipboard.writeText(url).then(() => {
            message.success('链接已复制到剪贴板');
        }).catch(() => {
            message.error('复制失败，请手动复制');
        });
    };

    // 加载状态
    if (loading) {
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
                <Layout.Content style={{
                    minHeight: 700,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    flexDirection: 'column'
                }}>
                    <Spin
                        size="large"
                        indicator={<LoadingOutlined style={{ fontSize: 48 }} spin />}
                    />
                    <p style={{ marginTop: 20, fontSize: 16, color: '#666' }}>
                        正在加载文献详情...
                    </p>
                    {queryTime && (
                        <p style={{ fontSize: 14, color: '#999' }}>
                            查询耗时: {queryTime}
                        </p>
                    )}
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

    // 错误状态
    if (error && !paper) {
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
                <Layout.Content style={{minHeight: 700, padding: '50px'}}>
                    <div style={{ maxWidth: 600, margin: '0 auto', textAlign: 'center' }}>
                        <Alert
                            message="加载失败"
                            description={error}
                            type="error"
                            showIcon
                            style={{ marginBottom: 24 }}
                        />
                        <Space size="large">
                            <Button
                                type="primary"
                                icon={<ArrowLeftOutlined />}
                                onClick={handleGoBack}
                                style={{ backgroundColor: '#b82e28' }}
                            >
                                返回上一页
                            </Button>
                            {wosUid && (
                                <Button
                                    icon={<ReloadOutlined />}
                                    onClick={handleReload}
                                >
                                    重新加载
                                </Button>
                            )}
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
                    padding: 0
                }}>
                    <Footer/>
                </Layout.Footer>
            </Layout>
        );
    }

    // 正常显示
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
                {/* 面包屑导航增强 */}
                <div style={{
                    padding: '16px 20px',
                    backgroundColor: 'white',
                    borderBottom: '1px solid #f0f0f0',
                    position: 'sticky',
                    top: 64,
                    zIndex: 5,
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                }}>
                    <Space>
                        <Button
                            type="text"
                            icon={<ArrowLeftOutlined />}
                            onClick={handleGoBack}
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
                        {location.state?.searchPage && (
                            <>
                                <span style={{ color: '#666' }}>|</span>
                                <span style={{ color: '#999', fontSize: 14 }}>
                                    来自搜索结果第 {location.state.searchPage} 页
                                </span>
                            </>
                        )}
                    </Space>

                    {/* 新增：操作按钮 */}
                    <Space>
                        {wosUid && (
                            <>
                                <Tooltip title="重新加载">
                                    <Button
                                        type="text"
                                        icon={<ReloadOutlined />}
                                        onClick={handleReload}
                                        style={{ color: '#666' }}
                                    />
                                </Tooltip>
                                <Tooltip title="复制链接">
                                    <Button
                                        type="text"
                                        icon={<LinkOutlined />}
                                        onClick={handleCopyLink}
                                        style={{ color: '#666' }}
                                    />
                                </Tooltip>
                            </>
                        )}
                        {paper?.wos_uid && (
                            <Tooltip title={`WOS_UID: ${paper.wos_uid}`}>
                                <Button
                                    type="text"
                                    icon={<CopyOutlined />}
                                    onClick={() => {
                                        navigator.clipboard.writeText(paper.wos_uid).then(() => {
                                            message.success('WOS_UID已复制');
                                        });
                                    }}
                                    style={{ color: '#666' }}
                                >
                                    复制ID
                                </Button>
                            </Tooltip>
                        )}
                    </Space>
                </div>

                <div className='paperDetailContent'>
                    {paper && <DetailCard paperInfo={paper}/>}
                </div>

                {/* 显示查询时间（如果有） */}
                {queryTime && (
                    <div style={{
                        textAlign: 'center',
                        padding: '10px',
                        color: '#999',
                        fontSize: '12px'
                    }}>
                        数据加载耗时: {queryTime}
                    </div>
                )}
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