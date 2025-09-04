import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Flex, Select, Input, Button, Divider, Tabs, Space, message, Spin } from 'antd';
import { PlusOutlined, SearchOutlined, ClearOutlined, UserOutlined, FileTextOutlined } from '@ant-design/icons';
import axios from 'axios';
import '../styles/searchInput.css';
import ConditionRow from "./conditionRow";

const { Option } = Select;
const { TabPane } = Tabs;

const SearchInput = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [searchFilter, setSearchFilter] = useState([
    { id: 1, selects: ['AND', 1], input: '' }
  ]);

  // Add a new search condition
  const handleAddFilter = () => {
    const newId = searchFilter.length + 1;
    setSearchFilter([...searchFilter, { id: newId, selects: ['AND', 1], input: '' }]);
  };

  // Clear all search conditions
  const handleClearAll = () => {
    setSearchFilter([{ id: 1, selects: ['AND', 1], input: '' }]);
  };

  // Delete a search condition
  const handleDeleteFilter = (filterId) => {
    const updatedFilters = searchFilter.filter(filter => filter.id !== filterId);
    const reassignedFilters = updatedFilters.map((filter, index) => (
        { ...filter, id: index + 1 }
    ));
    setSearchFilter(reassignedFilters);
  };

  // Update select value
  const handleSelectChange = (filterId, selectIndex, newValue) => {
    setSearchFilter(searchFilter.map(filter =>
        filter.id === filterId
            ? { ...filter, selects: filter.selects.map((value, index) =>
                  index === selectIndex ? newValue : value) }
            : filter
    ));
  };

  // Update input value
  const handleInputChange = (filterId, newValue) => {
    setSearchFilter(searchFilter.map(filter =>
        filter.id === filterId
            ? { ...filter, input: newValue }
            : filter
    ));
  };

  // Submit search request
  const handleSearch = async () => {
    // Validate inputs
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

      const paperInfo = response.data;
      // 传递搜索条件和结果到搜索结果页面
      navigate("/SearchResult", {
        state: {
          paperInfo,
          searchFilter // 新增：传递搜索条件
        }
      });
    } catch (error) {
      console.error("搜索请求失败:", error);
      message.error("搜索失败，请稍后重试");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    console.log(searchFilter);
  }, [searchFilter]);

  return (
      <div className="search-input-container">
        {/* Title section - now separate from tabs */}
        <div className="search-title">
          <h1 style={{ color: '#b82e28' }}>高级检索</h1>
          <p style={{ color: '#666' }}>使用多个条件组合，精确查找您需要的文献</p>
        </div>

        {/* Tabs section - now below the title */}
        <Tabs
            defaultActiveKey="1"
            type="card"
            size="large"
            className="search-tabs"
        >
          <TabPane
              tab={<span><FileTextOutlined />文献检索</span>}
              key="1"
          >
            <Spin spinning={loading}>
              <div className="search-panel">
                {/* 首行搜索条件 */}
                <div className="default-search">
                  <Select
                      style={{width: '40%', marginRight: '1%'}}
                      value={searchFilter[0].selects[1]}
                      onChange={(value) => handleSelectChange(1, 1, value)}
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
                      style={{width: '60%'}}
                      value={searchFilter[0].input}
                      onChange={(e) => handleInputChange(1, e.target.value)}
                  />
                </div>

                {/* 其他搜索条件 */}
                {searchFilter.slice(1).map(filter => (
                    <ConditionRow
                        key={filter.id}
                        filter={filter}
                        handleSelectChange={handleSelectChange}
                        handleInputChange={handleInputChange}
                        handleDeleteFilter={handleDeleteFilter}
                    />
                ))}

                <Divider style={{ margin: '16px 0' }} />

                <Flex className="action-buttons" justify="space-between">
                  <Button
                      type="dashed"
                      icon={<PlusOutlined />}
                      onClick={handleAddFilter}
                      style={{ borderColor: '#b82e28', color: '#b82e28' }}
                  >
                    添加条件
                  </Button>

                  <Space>
                    <Button
                        icon={<ClearOutlined />}
                        onClick={handleClearAll}
                    >
                      清空
                    </Button>
                    <Button
                        type="primary"
                        icon={<SearchOutlined />}
                        onClick={handleSearch}
                        style={{ background: '#b82e28' }}
                    >
                      搜索
                    </Button>
                  </Space>
                </Flex>
              </div>
            </Spin>
          </TabPane>

          <TabPane
              tab={<span><UserOutlined />作者检索</span>}
              key="2"
          >
            <div className="coming-soon">
              <h3>作者检索功能即将上线</h3>
              <p>请使用文献检索功能并选择"作者"字段进行搜索</p>
            </div>
          </TabPane>
        </Tabs>
      </div>
  );
};

export default SearchInput;