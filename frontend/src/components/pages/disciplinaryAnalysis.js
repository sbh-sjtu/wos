import React, { useState } from 'react';
import { Layout, Input, DatePicker, Space, Button, message, Spin, Card, Row, Col, Typography } from "antd";
import { SearchOutlined, CalendarOutlined, BarChartOutlined } from '@ant-design/icons';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, PieChart, Pie, Cell } from 'recharts';
import axios from 'axios';

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

    // 更新关键词input
    const onKeywordChange = (e) => {
        setKeyword(e.target.value);
    };

    // 更新日期范围
    const onDateRangeChange = (dates, dateStrings) => {
        setDateRange(dateStrings);
    };

    // 提交数据
    const handleSubmit = async () => {
        if (!keyword) {
            message.warning('请输入关键词');
            return;
        }

        if (dateRange.length !== 2) {
            message.warning('请选择日期范围');
            return;
        }

        const [startDate, endDate] = dateRange;
        setLoading(true);

        try {
            const response = await axios.post('http://localhost:8888/main2022/disciplinaryAnalysis', {
                keyword,
                startDate,
                endDate
            });

            if (response.status === 200) {
                message.success('分析请求已提交成功！');
                setAnalysisData(response.data);
            } else {
                message.error('提交数据失败。');
            }
        } catch (error) {
            console.error('Error submitting data:', error);
            message.error('提交数据时发生错误。');
        } finally {
            setLoading(false);
        }
    };

    // 按下回车键触发搜索
    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
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

                            <Spin spinning={loading}>
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
                                                />
                                            </div>
                                        </Col>
                                    </Row>

                                    <div style={{ textAlign: 'center', marginTop: '20px' }}>
                                        <Button
                                            type="primary"
                                            size="large"
                                            icon={<BarChartOutlined />}
                                            onClick={handleSubmit}
                                            style={{
                                                background: '#b82e28',
                                                borderColor: '#b82e28',
                                                minWidth: '120px',
                                                height: '40px'
                                            }}
                                        >
                                            开始分析
                                        </Button>
                                    </div>
                                </div>
                            </Spin>
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

                            <Row gutter={24}>
                                {/* 国家分布 */}
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

                                {/* 期刊分布 */}
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
                            </Row>

                            {/* 顶级作者和机构 */}
                            <Row gutter={24} style={{ marginTop: '24px' }}>
                                <Col xs={24} lg={12}>
                                    <Card>
                                        <Title level={4} style={{ color: '#b82e28' }}>
                                            高产作者 (Top 10)
                                        </Title>
                                        <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                                            {analysisData.authorAnalysis?.topAuthors &&
                                                Object.entries(analysisData.authorAnalysis.topAuthors)
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

                                <Col xs={24} lg={12}>
                                    <Card>
                                        <Title level={4} style={{ color: '#b82e28' }}>
                                            主要机构 (Top 10)
                                        </Title>
                                        <div style={{ maxHeight: '300px', overflowY: 'auto' }}>
                                            {analysisData.authorAnalysis?.topInstitutions &&
                                                Object.entries(analysisData.authorAnalysis.topInstitutions)
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
                        </div>
                    </div>
                )}
            </Content>
        </Layout>
    );
}

export default DisciplinaryAnalysis;