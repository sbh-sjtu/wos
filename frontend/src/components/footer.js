import React from 'react';
import { Col, Row, Divider, Space, Typography } from 'antd';
import { MailOutlined, EnvironmentOutlined, CopyrightOutlined, ClockCircleOutlined, EyeOutlined, DownloadOutlined, TeamOutlined, LineChartOutlined } from '@ant-design/icons';

const { Text, Title } = Typography;

const Footer = () => {
  // (Fake)统计数据（可以从API获取或通过props传入）
  const stats = {
    visits: '1,234,567',
    views: '3,456,789',
    downloads: '567,890',
    users: '12,345',
    lastUpdate: '2025-04-01'
  };

  const iconStyle = {
    fontSize: '18px',
    marginRight: '8px'
  };

  return (
    <footer style={{
      backgroundColor: '#b82e28',
      padding: '24px 40px',
      color: 'white',
    }}>
      {/* 统计信息部分 */}
      <Row gutter={[32, 16]} style={{ marginBottom: '20px' }}>
        <Col xs={24} sm={12} md={6}>
          <Space>
            <LineChartOutlined style={iconStyle} />
            <Text strong style={{ color: 'white', fontSize: '15px' }}>访问量：</Text>
            <Text style={{ color: 'white' }}>{stats.visits}</Text>
          </Space>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Space>
            <EyeOutlined style={iconStyle} />
            <Text strong style={{ color: 'white', fontSize: '15px' }}>浏览：</Text>
            <Text style={{ color: 'white' }}>{stats.views}</Text>
          </Space>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Space>
            <DownloadOutlined style={iconStyle} />
            <Text strong style={{ color: 'white', fontSize: '15px' }}>下载量：</Text>
            <Text style={{ color: 'white' }}>{stats.downloads}</Text>
          </Space>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Space>
            <TeamOutlined style={iconStyle} />
            <Text strong style={{ color: 'white', fontSize: '15px' }}>注册用户：</Text>
            <Text style={{ color: 'white' }}>{stats.users}</Text>
          </Space>
        </Col>
      </Row>

      <Divider style={{ background: 'rgba(255, 255, 255, 0.2)', margin: '12px 0' }} />

      {/* 联系信息部分 */}
      <Row gutter={[32, 16]} style={{ marginTop: '20px' }}>
        <Col xs={24} sm={12} md={6}>
          <Space>
            <CopyrightOutlined style={iconStyle} />
            <Text style={{ color: 'white' }}>版权所有：© 上海交通大学图书馆</Text>
          </Space>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Space>
            <EnvironmentOutlined style={iconStyle} />
            <Text style={{ color: 'white' }}>地址: 上海市东川路800号</Text>
          </Space>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Space>
            <MailOutlined style={iconStyle} />
            <Text style={{ color: 'white' }}>联系我们: ir-sjtu@sjtu.edu.cn</Text>
          </Space>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Space>
            <ClockCircleOutlined style={iconStyle} />
            <Text style={{ color: 'white' }}>更新时间：{stats.lastUpdate}</Text>
          </Space>
        </Col>
      </Row>
    </footer>
  );
};

export default Footer;