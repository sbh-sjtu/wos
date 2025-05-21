import React from 'react';
import '../styles/conditionRow.css';
import { Select, Input, Tooltip } from 'antd';
import { DeleteOutlined } from '@ant-design/icons';

const { Option } = Select;

function ConditionRow({ filter, handleSelectChange, handleInputChange, handleDeleteFilter }) {
    return (
        <div key={filter.id} className='condition'>
            {/* 删除键 */}
            <Tooltip title={'删除条件 '+filter.id}>
                <div 
                    className='delete-icon' 
                    onClick={() => handleDeleteFilter(filter.id)}
                >
                    <DeleteOutlined style={{ color: '#b82e28' }} />
                </div>
            </Tooltip>
            
            {/* 选择条件之间关系AND&OR */}
            <Select 
                style={{width: '10%', marginRight: '1%'}} 
                value={filter.selects[0]} 
                onChange={(value) => handleSelectChange(filter.id, 0, value)}
            >
                <Option value={"AND"}>AND</Option>
                <Option value={"OR"}>OR</Option>
            </Select>
            
            {/* 选择条件 */}
            <Select 
                style={{width: '23.5%', marginRight: '1%'}}
                value={filter.selects[1]}
                onChange={(value) => handleSelectChange(filter.id, 1, value)}
            >
                <Option value={1}>Topic</Option>
                <Option value={2}>Title</Option>
                <Option value={3}>Author</Option>
                <Option value={4}>Publication/Source Titles</Option>
                <Option value={5}>Year Published</Option>
                <Option value={6}>DOI</Option>
            </Select>
            
            <Input 
                style={{width: '65%'}}
                value={filter.input}
                placeholder="输入关键词..."
                onChange={(e) => handleInputChange(filter.id, e.target.value)}
            />
        </div>
    );
}

export default ConditionRow;