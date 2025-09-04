import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from 'axios';
import { Input, Button, message, Spin, Tooltip } from 'antd';
import { SearchOutlined, ArrowRightOutlined } from '@ant-design/icons';
import '../styles/fulltextSearch.css';

const FulltextSearch = () => {
  const navigate = useNavigate();
  const [searchText, setSearchText] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSearchChange = (e) => {
    setSearchText(e.target.value);
  };

  const handleFullTextSearch = async () => {
    // 检查搜索文本是否为空
    if (!searchText.trim()) {
      message.warning("请输入有效的搜索关键词");
      return;
    }

    setLoading(true);

    try {
      // 发送搜索请求
      const response = await axios.post(
          `http://localhost:8888/main2022/fullTextSearch?query=${encodeURIComponent(searchText)}`
      );

      const paperInfo = response.data;

      // 构建对应的搜索条件（用于在结果页面显示和修改）
      const searchFilter = [
        {
          id: 1,
          selects: ['AND', 1], // Topic搜索
          input: searchText
        }
      ];

      // 跳转到搜索结果页面，传递结果和搜索条件
      navigate("/SearchResult", {
        state: {
          paperInfo,
          searchFilter // 传递搜索条件以便在结果页修改
        }
      });
    } catch (error) {
      console.error("搜索请求失败:", error);
      message.error("搜索失败，请稍后重试");
    } finally {
      setLoading(false);
    }
  };

  // 按下回车键触发搜索
  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      handleFullTextSearch();
    }
  };

  return (
      <div className="fulltext-search-container">
        <div className="search-title">
          <h1 style={{ color: '#b82e28', fontWeight: 600 }}>文献全文检索</h1>
        </div>

        <div className="search-input-container">
          <Spin spinning={loading}>
            <div className="search-input-wrapper">
              <Input
                  size="large"
                  placeholder="请输入搜索关键词..."
                  value={searchText}
                  onChange={handleSearchChange}
                  onKeyDown={handleKeyDown}
                  prefix={<SearchOutlined style={{ color: '#b82e28' }} />}
                  style={{
                    width: '100%',
                    height: 50,
                    borderRadius: '8px 0 0 8px',
                    borderRight: 0
                  }}
              />
              <Button
                  type="primary"
                  size="large"
                  onClick={handleFullTextSearch}
                  style={{
                    height: 50,
                    width: 120,
                    background: '#b82e28',
                    borderRadius: '0 8px 8px 0'
                  }}
              >
                搜索
              </Button>
            </div>
          </Spin>

          <div className="advanced-search-link">
            <Tooltip title="使用高级搜索功能">
              <Button
                  type="link"
                  onClick={() => navigate('/advancedSearch')}
                  icon={<ArrowRightOutlined />}
                  style={{ color: '#b82e28', fontWeight: 500 }}
              >
                高级搜索
              </Button>
            </Tooltip>
          </div>
        </div>
      </div>
  );
};

export default FulltextSearch;