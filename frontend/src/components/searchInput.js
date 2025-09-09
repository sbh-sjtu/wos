import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Flex, Select, Input, Button, Divider, Space, message, Typography } from 'antd';
import { PlusOutlined, SearchOutlined, ClearOutlined, DownOutlined, UpOutlined } from '@ant-design/icons';
import axios from 'axios';
import '../styles/searchInput.css';
import ConditionRow from "./conditionRow";

const { Option } = Select;
const { Text } = Typography;

const SearchInput = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [searchFilter, setSearchFilter] = useState([
    { id: 1, selects: ['AND', 1], input: '' }
  ]);
  const [showAdvanced, setShowAdvanced] = useState(false);

  // Add a new search condition
  const handleAddFilter = () => {
    const newId = searchFilter.length + 1;
    setSearchFilter([...searchFilter, { id: newId, selects: ['AND', 1], input: '' }]);
    setShowAdvanced(true);
  };

  // Clear all search conditions
  const handleClearAll = () => {
    setSearchFilter([{ id: 1, selects: ['AND', 1], input: '' }]);
    setShowAdvanced(false);
  };

  // Delete a search condition
  const handleDeleteFilter = (filterId) => {
    if (searchFilter.length === 1) return;

    const updatedFilters = searchFilter.filter(filter => filter.id !== filterId);
    const reassignedFilters = updatedFilters.map((filter, index) => (
        { ...filter, id: index + 1 }
    ));
    setSearchFilter(reassignedFilters);

    if (reassignedFilters.length === 1) {
      setShowAdvanced(false);
    }
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

  // Handle Enter key press
  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !loading) {
      handleSearch();
    }
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

      if (paperInfo.length >= 500) {
        message.success(`搜索完成，当前显示前 500 条结果`);
      } else {
        message.success(`找到 ${paperInfo.length} 篇文献`);
      }

      // 传递搜索条件和结果到搜索结果页面
      navigate("/searchResult", {
        state: {
          paperInfo,
          searchFilter
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
        {/* 标题区域 */}
        <div className="search-title">
          <h1>文献检索</h1>
          <p>支持多条件组合搜索，精确查找您需要的文献</p>
        </div>

        {/* 搜索面板 */}
        <div className="search-panel">
          {/* 主搜索框 */}
          <div className="main-search-row">
            <Select
                className="field-select"
                value={searchFilter[0].selects[1]}
                onChange={(value) => handleSelectChange(1, 1, value)}
                size="large"
                disabled={loading}
            >
              <Option value={1}>Topic</Option>
              <Option value={2}>Title</Option>
              <Option value={3}>Author</Option>
              <Option value={4}>Publication/Source Titles</Option>
              <Option value={5}>Year Published</Option>
              <Option value={6}>DOI</Option>
            </Select>

            <Input
                className="search-input"
                placeholder="输入关键词..."
                value={searchFilter[0].input}
                onChange={(e) => handleInputChange(1, e.target.value)}
                onKeyDown={handleKeyDown}
                size="large"
                disabled={loading}
            />

            <Button
                type="primary"
                icon={<SearchOutlined />}
                onClick={handleSearch}
                loading={loading}
                className="search-button"
                size="large"
            >
              搜索
            </Button>
          </div>

          {/* 操作按钮行 */}
          <div className="action-buttons-row">
            <Button
                type="link"
                icon={showAdvanced ? <UpOutlined /> : <DownOutlined />}
                onClick={() => setShowAdvanced(!showAdvanced)}
                disabled={loading}
                className="toggle-button"
            >
              {showAdvanced ? '收起高级选项' : '展开高级选项'}
            </Button>

            <Button
                type="link"
                icon={<PlusOutlined />}
                onClick={handleAddFilter}
                disabled={loading}
                className="add-button"
            >
              添加搜索条件
            </Button>
          </div>

          {/* 高级搜索区域 */}
          {(showAdvanced || searchFilter.length > 1) && (
              <div className="advanced-section">
                <Divider>
                  <Text type="secondary">高级搜索选项</Text>
                </Divider>

                {/* 额外搜索条件 */}
                {searchFilter.slice(1).map(filter => (
                    <ConditionRow
                        key={filter.id}
                        filter={filter}
                        handleSelectChange={handleSelectChange}
                        handleInputChange={handleInputChange}
                        handleDeleteFilter={handleDeleteFilter}
                    />
                ))}

                {/* 高级操作按钮 */}
                <div className="advanced-actions">
                  <Button
                      type="dashed"
                      icon={<PlusOutlined />}
                      onClick={handleAddFilter}
                      disabled={loading}
                      className="add-condition-btn"
                  >
                    添加条件
                  </Button>

                  <Space>
                    <Button
                        icon={<ClearOutlined />}
                        onClick={handleClearAll}
                        disabled={loading}
                    >
                      清空所有
                    </Button>
                    <Button
                        type="primary"
                        icon={<SearchOutlined />}
                        onClick={handleSearch}
                        loading={loading}
                        className="execute-search-btn"
                    >
                      执行搜索
                    </Button>
                  </Space>
                </div>
              </div>
          )}

          {/* 搜索提示 */}
          <div className="search-tips">
            <Text type="secondary">
              搜索提示：Topic包含标题和关键词 | 支持AND/OR逻辑组合 | 年份支持范围查询(如:2020-2023) | 未指定年份时默认搜索2020年数据
            </Text>
          </div>
        </div>
      </div>
  );
};

export default SearchInput;