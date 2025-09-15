import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {Select, Input, Button, Divider, Space, message, Typography, Row, Col} from 'antd';
import { PlusOutlined, SearchOutlined, ClearOutlined, DownOutlined, UpOutlined, CalendarOutlined } from '@ant-design/icons';
import axios from 'axios';
import '../styles/searchInput.css';
import ConditionRow from "./conditionRow";

const { Option } = Select;
const { Text } = Typography;

const SearchInput = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [selectedYear, setSelectedYear] = useState('2020'); // 默认年份
  const [searchFilter, setSearchFilter] = useState([
    { id: 1, selects: ['AND', 1], input: '' }
  ]);
  const [showAdvanced, setShowAdvanced] = useState(false);

  // 生成年份选项（1950-2020）
  const generateYearOptions = () => {
    const years = [];
    for (let year = 2020; year >= 1950; year--) {
      years.push(year.toString());
    }
    return years;
  };

  const yearOptions = generateYearOptions();

  // Add a new search condition
  const handleAddFilter = () => {
    const newId = searchFilter.length + 1;
    setSearchFilter([...searchFilter, { id: newId, selects: ['AND', 1], input: '' }]);
    setShowAdvanced(true);
  };

  // Clear all search conditions
  const handleClearAll = () => {
    setSearchFilter([{ id: 1, selects: ['AND', 1], input: '' }]);
    setSelectedYear('2020'); // 重置年份为默认值
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

  // Toggle advanced search visibility
  const handleToggleAdvanced = () => {
    setShowAdvanced(!showAdvanced);
  };

  // Submit search request - 修改为包含年份
  const handleSearch = async () => {
    // Validate inputs
    const emptyFields = searchFilter.filter(filter => !filter.input.trim());
    if (emptyFields.length > 0) {
      message.warning("请完整填写搜索条件");
      return;
    }

    setLoading(true);

    try {
      // 构建包含年份的搜索过滤器
      const searchWithYear = [...searchFilter];

      // 添加年份条件（作为最后一个条件，使用AND连接）
      searchWithYear.push({
        id: searchWithYear.length + 1,
        selects: ['AND', 5], // 5 = Year Published
        input: selectedYear
      });

      console.log('搜索条件（包含年份）:', searchWithYear);

      const response = await axios.post(
          "http://localhost:8888/main2022/advancedSearch",
          searchWithYear
      );

      const paperInfo = response.data;

      if (paperInfo.length >= 200) {
        message.success(`在${selectedYear}年找到${paperInfo.length}条结果（显示前200条）`);
      } else if (paperInfo.length > 0) {
        message.success(`在${selectedYear}年找到 ${paperInfo.length} 篇文献`);
      } else {
        message.info(`在${selectedYear}年未找到相关文献`);
      }

      // 传递搜索条件和结果到搜索结果页面
      navigate("/searchResult", {
        state: {
          paperInfo,
          searchFilter: searchWithYear // 传递包含年份的完整搜索条件
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
    console.log('当前搜索条件:', searchFilter);
    console.log('选择的年份:', selectedYear);
  }, [searchFilter, selectedYear]);

  return (
      <div className="search-input-container">
        {/* 标题区域 */}
        <div className="search-title">
          <h1>文献检索</h1>
          <p>支持多条件组合搜索，精确查找您需要的文献</p>
        </div>

        {/* 搜索面板 */}
        <div className="search-panel">
          {/* 主搜索行 - 添加年份选择器 */}
          <div className="main-search-section">
            <Row gutter={[16, 16]}>
              <Col xs={24} lg={18}>
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
                </div>
              </Col>

              <Col xs={24} lg={6}>
                <div className="year-search-group">
                  <Select
                      className="year-select"
                      value={selectedYear}
                      onChange={setSelectedYear}
                      size="large"
                      disabled={loading}
                      placeholder="选择年份"
                      suffixIcon={<CalendarOutlined style={{ color: '#b82e28' }} />}
                      showSearch
                      optionFilterProp="children"
                  >
                    {yearOptions.map(year => (
                        <Option key={year} value={year}>
                          {year}年
                        </Option>
                    ))}
                  </Select>

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
              </Col>
            </Row>
          </div>

          {/* 简化的操作按钮行 */}
          <div className="action-buttons-row">
            {searchFilter.length === 1 && (
                <Button
                    type="link"
                    icon={<PlusOutlined />}
                    onClick={handleAddFilter}
                    disabled={loading}
                    className="add-button"
                >
                  添加条件
                </Button>
            )}

            {searchFilter.length > 1 && (
                <Button
                    type="link"
                    icon={showAdvanced ? <UpOutlined /> : <DownOutlined />}
                    onClick={handleToggleAdvanced}
                    disabled={loading}
                    className="toggle-button"
                >
                  {showAdvanced ? '收起高级选项' : '展开高级选项'}
                </Button>
            )}
          </div>

          {/* 高级搜索区域 - 只在有多个条件且展开时显示 */}
          {searchFilter.length > 1 && showAdvanced && (
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

                {/* 简化的高级操作按钮 */}
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

                  <Button
                      icon={<ClearOutlined />}
                      onClick={handleClearAll}
                      disabled={loading}
                  >
                    清空所有
                  </Button>
                </div>
              </div>
          )}

          {/* 搜索提示 - 更新提示文本 */}
          <div className="search-tips">
            <Text type="secondary">
              搜索提示：Topic包含标题、关键词和摘要 | 支持AND/OR逻辑组合 | 当前搜索年份：<strong>{selectedYear}年</strong> | 支持1950-2020年数据
            </Text>
          </div>
        </div>
      </div>
  );
};

export default SearchInput;