import React, { useState } from 'react';
import { Layout, Input, DatePicker, Space, Button, message, Spin } from "antd";
import { SearchOutlined, CalendarOutlined, BarChartOutlined } from '@ant-design/icons';
import Header from '../header';
import Footer from '../footer';
import axios from 'axios';
import '../../styles/disciplinaryAnalysis.css';

const { RangePicker } = DatePicker;
const { Content } = Layout;

// 学科分析页面
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
        setDateRange(dateStrings); // Handle date range changes
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
            // 发送数据
            const response = await axios.post('http://localhost:20002/main2022/disciplinaryAnalysis', {
                keyword,
                startDate,
                endDate
            });

            if (response.status === 200) {
                message.success('分析请求已提交成功！');
                setAnalysisData(response.data);
                console.log('Response:', response.data);
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

    return (
        <Layout className='disciplinary-layout'>
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
            
            <Content>
                <div className="discipline-hero-section" style={{ backgroundColor: '#b82e28', paddingTop: '40px', paddingBottom: '80px' }}>
                    <div className="container">
                        <div className="discipline-analysis-container">
                            <div className="search-title">
                                <h1>学科分析</h1>
                                <p>基于关键词和时间维度分析学科发展趋势</p>
                            </div>
                            
                            <Spin spinning={loading}>
                                <div className="discipline-form">
                                    <div className="discipline-input-group">
                                        <div className="discipline-input-item">
                                            <label>关键词</label>
                                            <Input 
                                                placeholder="输入研究领域关键词..." 
                                                onChange={onKeywordChange}
                                                onKeyDown={handleKeyDown}
                                                prefix={<SearchOutlined style={{ color: '#b82e28' }} />}
                                                size="large"
                                                value={keyword}
                                            />
                                        </div>
                                        
                                        <div className="discipline-input-item">
                                            <label>时间范围</label>
                                            <RangePicker 
                                                picker="year" 
                                                onChange={onDateRangeChange}
                                                size="large"
                                                style={{ width: '100%' }}
                                                placeholder={['起始年份', '结束年份']}
                                                suffixIcon={<CalendarOutlined style={{ color: '#b82e28' }} />}
                                            />
                                        </div>
                                    </div>
                                    
                                    <div className="discipline-button-group">
                                        <Button 
                                            type="primary"
                                            size="large"
                                            icon={<BarChartOutlined />}
                                            onClick={handleSubmit}
                                            style={{ 
                                                background: '#b82e28', 
                                                borderColor: '#b82e28',
                                                minWidth: '120px'
                                            }}
                                        >
                                            分析
                                        </Button>
                                    </div>
                                </div>
                            </Spin>
                            
                            {analysisData && (
                                <div className="analysis-results">
                                    {/* 这里可以显示分析结果，如图表等 */}
                                    <div className="result-summary">
                                        <h3>分析结果摘要</h3>
                                        <p>关键词: {keyword}</p>
                                        <p>时间范围: {dateRange[0]} - {dateRange[1]}</p>
                                    </div>
                                </div>
                            )}
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
                <Footer/>
            </Layout.Footer>
        </Layout>
    );
}

export default DisciplinaryAnalysis;