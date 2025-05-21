import React, { useState } from 'react';
import { Flex, Menu, Typography } from "antd";
import { SearchOutlined, UserOutlined, BookOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';

// Navigation items configuration
const navigationItems = [
  {
    label: '文献检索',
    key: '1',
    icon: <SearchOutlined style={{ fontSize: 18 }} />,
    path: '/'
  },
  {
    label: '学科分析',
    key: '2',
    icon: <BookOutlined style={{ fontSize: 18 }} />,
    path: '/disciplinaryAnalysis'
  },
  {
    label: '用户',
    key: '3',
    icon: <UserOutlined style={{ fontSize: 18 }} />,
    // 未实现功能
  }
];

const Header = () => {
  const [currentKey, setCurrentKey] = useState('1');
  const navigate = useNavigate();
  const { Title } = Typography;

  const handleNavigation = (e) => {
    const { key } = e;
    setCurrentKey(key);
    
    const selectedItem = navigationItems.find(item => item.key === key);
    if (selectedItem?.path) {
      navigate(selectedItem.path);
    }
  };

  return (
    <Flex 
      className="header" 
      align="center" 
      justify="space-between"
      style={{
        padding: '0 24px',
        boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)',
        background: 'white',
        borderBottom: '1px solid #f0f0f0',
        height: 64
      }}
    >
      <Flex align="center" gap={12}>
        <div className="logo" style={{ display: 'flex', alignItems: 'center' }}>
          <div style={{ 
            width: 40, 
            height: 40, 
            background: '#b82e28', 
            borderRadius: '50%',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            marginRight: 12,
            color: 'white',
            fontWeight: 'bold',
            fontSize: 18
          }}>
            SJTU
          </div>
          <Title 
            level={4} 
            style={{ 
              margin: 0,
              fontSize: 18,
              fontWeight: 600,
              color: '#b82e28',
              fontWeight: 600
            }}
          >
            上海交通大学图书馆WOS
          </Title>
        </div>
      </Flex>
      
      <Menu 
        onClick={handleNavigation}
        selectedKeys={[currentKey]}
        mode="horizontal"
        items={navigationItems}
        style={{
          fontSize: 16,
          fontWeight: 500,
          minWidth: 500,
          border: 'none',
          background: 'transparent'
        }}
        className="header-menu"
      />
    </Flex>
  );
};

export default Header;