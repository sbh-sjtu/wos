import React, { useState } from 'react';
import { Layout, Input, DatePicker, Space, Button, message, Spin, Card, Row, Col, Typography, Progress, Alert } from "antd";
import { SearchOutlined, CalendarOutlined, BarChartOutlined } from '@ant-design/icons';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, PieChart, Pie, Cell } from 'recharts';
import axios from 'axios';
import Header from '../header';

const { RangePicker } = DatePicker;
const { Content } = Layout;
const { Title, Text } = Typography;

// 颜色配置
const COLORS = ['#b82e28', '#ff7875', '#ffa39e', '#ffb3b3', '#ffc1c1', '#ffd1d1', '#ffe1e1', '#fff1f0'];

function DisciplinaryAnalysis() {
    const [keyword, setKeyword] = useState('');
    const [dateRange, setDateRange] = useState([]);
    const [analysisData, setAnalysisData] = useState(null);
    const [loading, setLoading] = useState(false);
    const [progress, setProgress] = useState(0);
    const [error, setError] = useState(null);
    const [statusMessage, setStatusMessage] = useState('');

    // 更新关键词input
    const onKeywordChange = (e) => {
        setKeyword(e.target.value);
    };

    // 更新日期范围
    const onDateRangeChange = (dates, dateStrings) => {
        setDateRange(dateStrings);
    };

    // 改进的进度更新 - 更加详细的状态说明
    const updateProgress = () => {
        const steps = [
            { percent: 5, message: '正在初始化分析任务...', duration: 1000 },
            { percent: 15, message: '正在连接数据库...', duration: 2000 },
            { percent: 25, message: '正在查询相关文献数据...', duration: 3000 },
            { percent: 35, message: '正在处理多年份数据...', duration: 4000 },
            { percent: 50, message: '正在分析年度发展趋势...', duration: 2000 },
            { percent: 65, message: '正在处理国家和地区分布...', duration: 2000 },
            { percent: 75, message: '正在分析作者和机构信息...', duration: 2000 },
            { percent: 85, message: '正在处理期刊和关键词数据...', duration: 2000 },
            { percent: 95, message: '正在生成可视化图表...', duration: 1000 },
            { percent: 100, message: '分析完成！', duration: 500 }
        ];

        let currentStep = 0;

        const updateStep = () => {
            if (currentStep < steps.length) {
                const step = steps[currentStep];
                setProgress(step.percent);
                setStatusMessage(step.message);

                // 显示消息提示（前几步显示info，后面的步骤不显示以免过于频繁）
                if (currentStep < 6) {
                    message.info(step.message, 2);
                }

                currentStep++;

                // 为最后几步设置更长的延迟，因为数据处理可能需要更多时间
                const delay = currentStep > 6 ? step.duration * 1.5 : step.duration;
                setTimeout(updateStep, delay);
            }
        };

        updateStep();
    };

    // 提交数据
    const handleSubmit = async () => {
        if (!keyword.trim()) {
            message.warning('请输入关键词');
            return;
        }

        if (dateRange.length !== 2) {
            message.warning('请选择日期范围');
            return;
        }

        const [startDate, endDate] = dateRange;

        // 验证年份范围
        const start = parseInt(startDate);
        const end = parseInt(endDate);
        if (start > end) {
            message.warning('开始年份不能大于结束年份');
            return;
        }

        // 提醒用户大范围查询可能需要较长时间
        const yearRange = end - start + 1;
        if (yearRange > 5) {
            message.info(`您查询的年份跨度较大（${yearRange}年），分析可能需要较长时间，请耐心等待...`, 5);
        } else if (yearRange > 2) {
            message.info(`正在分析${yearRange}年的数据，请稍候...`, 3);
        }

        setLoading(true);
        setProgress(0);
        setError(null);
        setAnalysisData(null);
        setStatusMessage('正在启动分析...');

        // 开始进度更新
        updateProgress();

        try {
            const response = await axios.post('http://localhost:8888/main2022/disciplinaryAnalysis', {
                keyword: keyword.trim(),
                startDate,
                endDate
            }, {
                timeout: 600000, // 增加到10分钟超时
                onUploadProgress: (progressEvent) => {
                    // 上传进度
                    console.log('Upload progress:', progressEvent);
                },
                // 添加请求拦截器来显示更详细的状态
                validateStatus: function (status) {
                    return status >= 200 && status < 300; // 默认的
                }
            });

            if (response.status === 200) {
                if (response.data.error) {
                    setError(response.data.error);
                    message.error(response.data.error, 5);
                } else if (response.data.message) {
                    message.warning(response.data.message, 4);
                    setAnalysisData(response.data);
                } else {
                    message.success('分析完成！', 3);
                    setAnalysisData(response.data);
                }
                setProgress(100);
                setStatusMessage('分析完成！');
            } else {
                throw new Error(`服务器响应错误: ${response.status}`);
            }
        } catch (error) {
            console.error('Error submitting data:', error);
            setProgress(0);
            setStatusMessage('');

            // 更详细的错误处理
            if (error.code === 'ECONNABORTED') {
                setError('分析超时，这可能是因为数据量较大或查询条件复杂。请尝试：1. 缩小年份范围；2. 使用更具体的关键词；3. 稍后重试');
                message.error('分析超时，请尝试缩小查询范围或稍后重试', 6);
            } else if (error.response?.status >= 500) {
                setError('服务器处理超时或内部错误，请稍后重试');
                message.error('服务器处理超时，请稍后重试', 5);
            } else if (error.response?.status === 400) {
                setError('请求参数错误，请检查输入的关键词和年份范围');
                message.error('请求参数错误，请检查输入', 4);
            } else if (error.message.includes('Network Error')) {
                setError('网络连接错误，请检查网络连接或后端服务状态');
                message.error('网络连接错误，请检查后端服务是否启动', 5);
            } else if (error.response?.status === 504) {
                setError('服务器网关超时，数据处理时间过长，请尝试缩小查询范围');
                message.error('处理超时，请尝试缩小查询范围', 6);
            } else {
                setError('分析失败: ' + (error.message || '未知错误'));
                message.error('分析失败，请稍后重试', 4);
            }
        } finally {
            setLoading(false);
        }
    };

    // 按下回车键触发搜索
    const handleKeyDown = (e) => {
        if (e.key === 'Enter' && !loading) {
            handleSubmit();
        }
    };

    // 处理年度趋势数据
    const processYearlyData = (data) => {
        if (!data) return [];
        return Object.entries(data).map(([year, count]) => ({
            year,
            count: count
        }));
    };

    // 处理国家分布数据
    const processCountryData = (data) => {
        if (!data) return [];
        return Object.entries(data)
            .slice(0, 10)
            .map(([country, count]) => ({
                country,
                count: count
            }));
    };

    // 处理期刊分布数据
    const processJournalData = (data) => {
        if (!data) return [];
        return Object.entries(data)
            .slice(0, 8)
            .map(([journal, count], index) => ({
                journal: journal.length > 30 ? journal.substring(0, 30) + '...' : journal,
                count: count,
                fill: COLORS[index % COLORS.length]
            }));
    };

    return (
        <Layout className='disciplinary-layout' style={{ minHeight: '100vh' }}>
            <Header />
            <Content>
                <div style={{
                    backgroundColor: '#b82e28',
                    padding: '40px 20px 80px',
                    background: 'linear-gradient(135deg, #b82e28 0%, #991f1b 100%)'
                }}>
                    <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
                        <div style={{
                            backgroundColor: 'white',
                            padding: '40px',
                            borderRadius: '12px',
                            boxShadow: '0 4px 20px rgba(0, 0, 0, 0.15)'
                        }}>
                            <div style={{ textAlign: 'center', marginBottom: '30px' }}>
                                <Title level={2} style={{ color: '#b82e28', marginBottom: '8px' }}>
                                    学科分析
                                </Title>
                                <Text style={{ color: '#666' }}>
                                    基于关键词和时间维度分析学科发展趋势
                                </Text>
                            </div>

                            {/* 错误提示 */}
                            {error && (
                                <Alert
                                    message="分析失败"
                                    description={error}
                                    type="error"
                                    showIcon
                                    closable
                                    onClose={() => setError(null)}
                                    style={{ marginBottom: '20px' }}
                                />
                            )}

                            <div style={{ marginBottom: '30px' }}>
                                <Row gutter={24}>
                                    <Col xs={24} md={12}>
                                        <div style={{ marginBottom: '20px' }}>
                                            <Text strong>关键词</Text>
                                            <Input
                                                placeholder="输入研究领域关键词..."
                                                onChange={onKeywordChange}
                                                onKeyDown={handleKeyDown}
                                                prefix={<SearchOutlined style={{ color: '#b82e28' }} />}
                                                size="large"
                                                value={keyword}
                                                style={{ marginTop: '8px' }}
                                                disabled={loading}
                                            />
                                        </div>
                                    </Col>

                                    <Col xs={24} md={12}>
                                        <div style={{ marginBottom: '20px' }}>
                                            <Text strong>时间范围</Text>
                                            <RangePicker
                                                picker="year"
                                                onChange={onDateRangeChange}
                                                size="large"
                                                style={{ width: '100%', marginTop: '8px' }}
                                                placeholder={['起始年份', '结束年份']}
                                                suffixIcon={<CalendarOutlined style={{ color: '#b82e28' }} />}
                                                disabled={loading}
                                            />
                                        </div>
                                    </Col>
                                </Row>

                                {/* 优化的进度显示 */}
                                {loading && (
                                    <div style={{ marginBottom: '20px' }}>
                                        <Progress
                                            percent={progress}
                                            status="active"
                                            strokeColor={{
                                                '0%': '#b82e28',
                                                '100%': '#ff7875',
                                            }}
                                            showInfo={true}
                                            format={() => `${progress}%`}
                                        />
                                        <div style={{
                                            marginTop: '12px',
                                            textAlign: 'center',
                                            padding: '8px 16px',
                                            backgroundColor: '#f6f8fa',
                                            borderRadius: '6px',
                                            border: '1px solid #e1e8ed'
                                        }}>
                                            <Text strong style={{ color: '#b82e28', fontSize: '14px' }}>
                                                {statusMessage}
                                            </Text>
                                            <br />
                                            <Text style={{ fontSize: '12px', color: '#666', marginTop: '4px' }}>
                                                分析可能需要数分钟时间，请耐心等待...
                                            </Text>
                                        </div>
                                    </div>
                                )}

                                {/* 查询提示 */}
                                {!loading && (
                                    <Alert
                                        message="查询提示"
                                        description="• 大范围年份查询（5年以上）可能需要较长时间（2-5分钟）
                                                   • 建议使用具体的关键词以获得更精确的结果
                                                   • 如遇超时，请尝试缩小年份范围或使用更具体的关键词"
                                        type="info"
                                        showIcon
                                        style={{ marginBottom: '20px' }}
                                    />
                                )}

                                <div style={{ textAlign: 'center', marginTop: '20px' }}>
                                    <Button
                                        type="primary"
                                        size="large"
                                        icon={<BarChartOutlined />}
                                        onClick={handleSubmit}
                                        loading={loading}
                                        style={{
                                            background: loading ? '#ccc' : '#b82e28',
                                            borderColor: loading ? '#ccc' : '#b82e28',
                                            minWidth: '140px',
                                            height: '42px',
                                            fontSize: '16px',
                                            fontWeight: '600'
                                        }}
                                        disabled={loading}
                                    >
                                        {loading ? '分析中...' : '开始分析'}
                                    </Button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* 分析结果展示区域 */}
                {analysisData && (
                    <div style={{
                        backgroundColor: '#f5f5f5',
                        padding: '40px 20px',
                        minHeight: 'calc(100vh - 400px)'
                    }}>
                        <div style={{ maxWidth: '1400px', margin: '0 auto' }}>
                            {/* 检查是否有数据 */}
                            {analysisData.summary?.totalPapers === 0 ? (
                                <Card>
                                    <Alert
                                        message="未找到相关数据"
                                        description={`在指定的时间范围内未找到与关键词"${keyword}"相关的论文。请尝试：1. 修改关键词；2. 扩大时间范围；3. 使用更通用的关键词。`}
                                        type="info"
                                        showIcon
                                    />
                                </Card>
                            ) : (
                                <>
                                    {/* 摘要信息 */}
                                    <Card style={{ marginBottom: '24px' }}>
                                        <Title level={4} style={{ color: '#b82e28', marginBottom: '20px' }}>
                                            分析摘要
                                        </Title>
                                        <Row gutter={24}>
                                            <Col xs={12} sm={6}>
                                                <div style={{ textAlign: 'center' }}>
                                                    <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#b82e28' }}>
                                                        {analysisData.summary?.totalPapers || 0}
                                                    </div>
                                                    <div style={{ color: '#666' }}>总论文数</div>
                                                </div>
                                            </Col>
                                            <Col xs={12} sm={6}>
                                                <div style={{ textAlign: 'center' }}>
                                                    <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#b82e28' }}>
                                                        {analysisData.summary?.uniqueCountries || 0}
                                                    </div>
                                                    <div style={{ color: '#666' }}>涉及国家</div>
                                                </div>
                                            </Col>
                                            <Col xs={12} sm={6}>
                                                <div style={{ textAlign: 'center' }}>
                                                    <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#b82e28' }}>
                                                        {analysisData.summary?.uniqueJournals || 0}
                                                    </div>
                                                    <div style={{ color: '#666' }}>期刊数量</div>
                                                </div>
                                            </Col>
                                            <Col xs={12} sm={6}>
                                                <div style={{ textAlign: 'center' }}>
                                                    <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#b82e28' }}>
                                                        {analysisData.summary?.uniqueAuthors || 0}
                                                    </div>
                                                    <div style={{ color: '#666' }}>作者数量</div>
                                                </div>
                                            </Col>
                                        </Row>
                                    </Card>

                                    {/* 论文数量年度趋势 */}
                                    {analysisData.yearlyTrend && Object.keys(analysisData.yearlyTrend).length > 0 && (
                                        <Card style={{ marginBottom: '24px' }}>
                                            <Title level={4} style={{ color: '#b82e28' }}>
                                                论文发表趋势
                                            </Title>
                                            <ResponsiveContainer width="100%" height={300}>
                                                <LineChart data={processYearlyData(analysisData.yearlyTrend)}>
                                                    <CartesianGrid strokeDasharray="3 3" />
                                                    <XAxis dataKey="year" />
                                                    <YAxis />
                                                    <Tooltip />
                                                    <Line
                                                        type="monotone"
                                                        dataKey="count"
                                                        stroke="#b82e28"
                                                        strokeWidth={3}
                                                        dot={{ fill: '#b82e28', strokeWidth: 2, r: 5 }}
                                                    />
                                                </LineChart>
                                            </ResponsiveContainer>
                                        </Card>
                                    )}

                                    <Row gutter={24}>
                                        {/* 国家分布 */}
                                        {analysisData.countryDistribution && Object.keys(analysisData.countryDistribution).length > 0 && (
                                            <Col xs={24} lg={12}>
                                                <Card style={{ height: '450px' }}>
                                                    <Title level={4} style={{ color: '#b82e28' }}>
                                                        国家分布 (Top 10)
                                                    </Title>
                                                    <ResponsiveContainer width="100%" height={350}>
                                                        <BarChart data={processCountryData(analysisData.countryDistribution)}>
                                                            <CartesianGrid strokeDasharray="3 3" />
                                                            <XAxis
                                                                dataKey="country"
                                                                angle={-45}
                                                                textAnchor="end"
                                                                height={100}
                                                                interval={0}
                                                            />
                                                            <YAxis />
                                                            <Tooltip />
                                                            <Bar dataKey="count" fill="#b82e28" />
                                                        </BarChart>
                                                    </ResponsiveContainer>
                                                </Card>
                                            </Col>
                                        )}

                                        {/* 期刊分布 */}
                                        {analysisData.journalDistribution && Object.keys(analysisData.journalDistribution).length > 0 && (
                                            <Col xs={24} lg={12}>
                                                <Card style={{ height: '450px' }}>
                                                    <Title level={4} style={{ color: '#b82e28' }}>
                                                        主要期刊分布
                                                    </Title>
                                                    <ResponsiveContainer width="100%" height={350}>
                                                        <PieChart>
                                                            <Pie
                                                                data={processJournalData(analysisData.journalDistribution)}
                                                                cx="50%"
                                                                cy="50%"
                                                                outerRadius={100}
                                                                dataKey="count"
                                                                label={({journal, percent}) =>
                                                                    `${journal}: ${(percent * 100).toFixed(0)}%`
                                                                }
                                                            >
                                                                {processJournalData(analysisData.journalDistribution).map((entry, index) => (
                                                                    <Cell key={`cell-${index}`} fill={entry.fill} />
                                                                ))}
                                                            </Pie>
                                                            <Tooltip />
                                                        </PieChart>
                                                    </ResponsiveContainer>
                                                </Card>
                                            </Col>
                                        )}
                                    </Row>

                                    {/* 顶级作者和机构 */}
                                    <Row gutter={24} style={{ marginTop: '24px' }}>
                                        {analysisData.authorAnalysis?.topAuthors && Object.keys(analysisData.authorAnalysis.topAuthors).length > 0 && (
                                            <Col xs={24} lg={12}>
                                                <Card>
                                                    <Title level={4} style={{ color: '#b82e28' }}>
                                                        高产作者 (Top 10)
                                                    </Title>
                                                    <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                                                        {Object.entries(analysisData.authorAnalysis.topAuthors)
                                                            .map(([author, count], index) => (
                                                                <div key={index} style={{
                                                                    display: 'flex',
                                                                    justifyContent: 'space-between',
                                                                    alignItems: 'center',
                                                                    padding: '8px 0',
                                                                    borderBottom: '1px solid #f0f0f0'
                                                                }}>
                                                                    <Text style={{ flex: 1, marginRight: '10px' }}>
                                                                        {author.length > 30 ? author.substring(0, 30) + '...' : author}
                                                                    </Text>
                                                                    <Text strong style={{ color: '#b82e28' }}>{count}</Text>
                                                                </div>
                                                            ))}
                                                    </div>
                                                </Card>
                                            </Col>
                                        )}

                                        {analysisData.authorAnalysis?.topInstitutions && Object.keys(analysisData.authorAnalysis.topInstitutions).length > 0 && (
                                            <Col xs={24} lg={12}>
                                                <Card>
                                                    <Title level={4} style={{ color: '#b82e28' }}>
                                                        主要机构 (Top 10)
                                                    </Title>
                                                    <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                                                        {Object.entries(analysisData.authorAnalysis.topInstitutions)
                                                            .map(([institution, count], index) => (
                                                                <div key={index} style={{
                                                                    display: 'flex',
                                                                    justifyContent: 'space-between',
                                                                    alignItems: 'center',
                                                                    padding: '8px 0',
                                                                    borderBottom: '1px solid #f0f0f0'
                                                                }}>
                                                                    <Text style={{ flex: 1, marginRight: '10px' }}>
                                                                        {institution.length > 40 ? institution.substring(0, 40) + '...' : institution}
                                                                    </Text>
                                                                    <Text strong style={{ color: '#b82e28' }}>{count}</Text>
                                                                </div>
                                                            ))}
                                                    </div>
                                                </Card>
                                            </Col>
                                        )}
                                    </Row>

                                    {/* 关键词趋势分析 */}
                                    {analysisData.keywordTrends && Object.keys(analysisData.keywordTrends).length > 0 && (
                                        <Card style={{ marginTop: '24px' }}>
                                            <Title level={4} style={{ color: '#b82e28' }}>
                                                关键词趋势分析
                                            </Title>
                                            <Row gutter={16}>
                                                {Object.entries(analysisData.keywordTrends).map(([year, keywords]) => (
                                                    <Col xs={24} sm={12} md={8} lg={6} key={year} style={{ marginBottom: '16px' }}>
                                                        <Card size="small" title={`${year}年`} style={{ height: '200px' }}>
                                                            <div style={{ maxHeight: '120px', overflowY: 'auto' }}>
                                                                {Object.entries(keywords).slice(0, 5).map(([keyword, count], index) => (
                                                                    <div key={index} style={{
                                                                        display: 'flex',
                                                                        justifyContent: 'space-between',
                                                                        marginBottom: '4px'
                                                                    }}>
                                                                        <Text style={{ fontSize: '12px' }}>
                                                                            {keyword.length > 15 ? keyword.substring(0, 15) + '...' : keyword}
                                                                        </Text>
                                                                        <Text strong style={{ fontSize: '12px', color: '#b82e28' }}>
                                                                            {count}
                                                                        </Text>
                                                                    </div>
                                                                ))}
                                                            </div>
                                                        </Card>
                                                    </Col>
                                                ))}
                                            </Row>
                                        </Card>
                                    )}
                                </>
                            )}
                        </div>
                    </div>
                )}
            </Content>
        </Layout>
    );
}

export default DisciplinaryAnalysis;