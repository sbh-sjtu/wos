import React from "react";
import '../styles/detailCard.css';
import { Card, Row, Col, Typography, Button, Tag, Space } from "antd";
import { LinkOutlined, GlobalOutlined, BankOutlined, UserOutlined } from '@ant-design/icons';

const { Title, Text, Paragraph } = Typography;

function DetailCard({ paperInfo }) {
    console.log(paperInfo);

    // HTML解码并保留特定标签的函数
    const decodeHtmlWithTags = (html) => {
        if (!html) return html;

        // 创建一个临时div来解析HTML
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = html;

        // 保留常见的格式标签，移除其他标签
        const allowedTags = ['i', 'em', 'b', 'strong', 'sup', 'sub', 'u'];
        const processNode = (node) => {
            if (node.nodeType === Node.TEXT_NODE) {
                return node.textContent;
            } else if (node.nodeType === Node.ELEMENT_NODE) {
                const tagName = node.tagName.toLowerCase();
                if (allowedTags.includes(tagName)) {
                    const content = Array.from(node.childNodes).map(processNode).join('');
                    return `<${tagName}>${content}</${tagName}>`;
                } else {
                    // 对于不允许的标签，只返回其内容
                    return Array.from(node.childNodes).map(processNode).join('');
                }
            }
            return '';
        };

        return Array.from(tempDiv.childNodes).map(processNode).join('');
    };

    // 简单的HTML解码函数（用于不需要保留标签的地方）
    const decodeHtml = (html) => {
        if (!html) return html;
        const txt = document.createElement("textarea");
        txt.innerHTML = html;
        return txt.value;
    };

    // 渲染带HTML标签的内容
    const renderHtmlContent = (content) => {
        if (!content) return content;
        const processedContent = decodeHtmlWithTags(content);
        return <span dangerouslySetInnerHTML={{ __html: processedContent }} />;
    };

    // 构建DOI链接
    const getDoiUrl = (doi) => {
        if (!doi) return null;
        return doi.startsWith('http') ? doi : `https://doi.org/${doi}`;
    };

    // 处理URL，优先使用原URL，其次使用DOI链接
    const getPaperUrl = () => {
        if (paperInfo.url && paperInfo.url.trim()) {
            return paperInfo.url;
        }
        return getDoiUrl(paperInfo.identifier_doi);
    };

    // 处理作者信息
    const getAuthorInfo = () => {
        const authors = paperInfo.author_fullname
            ? paperInfo.author_fullname.split(';').map(a => a.trim())
            : [];

        // 通讯作者 - 修复正则表达式
        let correspondingAuthors = [];
        if (paperInfo.reprint_address) {
            // 修复正则表达式，使用方括号匹配
            const matches = paperInfo.reprint_address.match(/\[(.*?)\]/g);
            if (matches) {
                correspondingAuthors = [...new Set(matches.map(m => m.replace(/[\[\]]/g, '').trim()))];
            }
        }

        // 机构和国家解析
        let institutionInfo = "暂无机构信息";
        let countryMap = new Map(); // 国家 → 作者集合

        if (paperInfo.address) {
            const institutionMap = new Map();
            const regex = /\[(.*?)\](.*?)(?=\[|$)/g;
            let match;
            while ((match = regex.exec(paperInfo.address)) !== null) {
                const authorsArr = match[1].split(/;|,/).map(a => a.trim()).filter(Boolean);
                const fullInstitution = match[2].trim().replace(/;$/, '');

                // 机构名：第一个逗号前
                const institution = fullInstitution.split(',')[0].trim();
                if (!institutionMap.has(institution)) institutionMap.set(institution, new Set());
                authorsArr.forEach(a => institutionMap.get(institution).add(a));

                // 国家：最后一个逗号后
                const country = fullInstitution.includes(',')
                    ? fullInstitution.split(',').pop().trim()
                    : fullInstitution;
                if (!countryMap.has(country)) countryMap.set(country, new Set());
                authorsArr.forEach(a => countryMap.get(country).add(a));
            }

            institutionInfo = Array.from(institutionMap.entries())
                .map(([inst, authorSet]) => `${inst} ： ${Array.from(authorSet).join(', ')}`)
                .join('\n');
        }

        return {
            allAuthors: paperInfo.author_fullname || "暂无作者信息",
            firstAuthor: authors.length > 0 ? authors[0] : "暂无信息",
            correspondingAuthor:
                correspondingAuthors.length > 0
                    ? correspondingAuthors.join(', ')
                    : "暂无信息",
            institution: institutionInfo,
            countryMap
        };
    };

    const colorPool = ['#f56a00', '#7265e6', '#ffbf00', '#00a2ae', '#13c2c2', '#2db7f5', '#87d068', '#ff7a45'];

    const colorMap = new Map(); // 机构或国家 -> 颜色
    let colorIndex = 0;

    const getColor = (key) => {
        if (!colorMap.has(key)) {
            colorMap.set(key, colorPool[colorIndex % colorPool.length]);
            colorIndex++;
        }
        return colorMap.get(key);
    };

    // 处理关键词信息
    const getKeywordInfo = () => {
        // 尝试多个可能的关键词字段
        const keywordFields = [
            paperInfo.keyword,
            paperInfo.keywords_plus,
            paperInfo.subj_group_micro_value,
            paperInfo.subject_extended
        ];

        for (let field of keywordFields) {
            if (field && field.trim()) {
                return field;
            }
        }
        return null;
    };

    // 处理PubMed ID
    const getPubMedId = () => {
        return paperInfo.identifier_pmid ||
            "暂无";
    };

    // 处理ISBN/EISBN
    const getIsbnInfo = () => {
        return paperInfo.identifier_isbn ||
            paperInfo.identifier_eisbn ||
            "暂无";
    };

    //处理出版商名称
    const getUnifiedPublisherName = (publisherStr) => {
        if (!publisherStr) return "暂无出版商信息";
        const match = publisherStr.match(/\[.*?;(.*?)\]/);
        return match ? match[1].trim() : publisherStr;
    };

    const authorInfo = getAuthorInfo();
    const paperUrl = getPaperUrl();
    const keywordInfo = getKeywordInfo();

    return (
        <div className="detail-container">
            <div className="detail-content">
                {/* 论文标题 */}
                <Card className="title-card" bodyStyle={{ padding: '24px' }}>
                    <Title level={2} className="paper-title">
                        {paperInfo.article_title ?
                            renderHtmlContent(paperInfo.article_title) :
                            "标题信息不可用"
                        }
                    </Title>
                </Card>

                {/* 作者信息和出版信息 */}
                <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
                    <Col xs={24} lg={12}>
                        <Card
                            title={
                                <span style={{ color: '#b82e28' }}>
                                    <UserOutlined style={{ marginRight: 8 }} />
                                    作者信息
                                </span>
                            }
                            className="info-card"
                        >
                            <div className="info-item">
                                <Text strong className="info-label">作者：</Text>
                                <Text>{renderHtmlContent(authorInfo.allAuthors)}</Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">第一作者：</Text>
                                <Text>{renderHtmlContent(authorInfo.firstAuthor)}</Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">通讯作者：</Text>
                                <Text>{renderHtmlContent(authorInfo.correspondingAuthor)}</Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">作者机构：</Text>
                                <div style={{ display: 'flex', flexDirection: 'column', marginLeft: 0 }}>
                                    {authorInfo.institution.split('\n').map((line, index) => {
                                        const instName = line.split('：')[0].trim();
                                        const color = getColor(instName);
                                        return (
                                            <div
                                                key={index}
                                                style={{
                                                    display: 'flex',
                                                    alignItems: 'flex-start',
                                                    marginBottom: 4,
                                                }}
                                            >
                                                <div
                                                    style={{
                                                        width: 12,
                                                        height: 12,
                                                        backgroundColor: color,
                                                        borderRadius: 2,
                                                        flexShrink: 0,
                                                        marginTop: 4,
                                                        marginRight: 8
                                                    }}
                                                />
                                                <Text style={{
                                                    margin: 0,
                                                    flex: 1,
                                                    lineHeight: '20px',
                                                    wordBreak: 'break-word',
                                                    whiteSpace: 'pre-wrap'
                                                }}>
                                                    {line}
                                                </Text>
                                            </div>
                                        );
                                    })}
                                </div>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">国家/地区：</Text>
                                <div style={{ display: 'flex', flexDirection: 'column', marginLeft: 0 }}>
                                    {Array.from(authorInfo.countryMap.entries()).map(([country, authorSet], index) => {
                                        const color = getColor(country);
                                        return (
                                            <div
                                                key={index}
                                                style={{
                                                    display: 'flex',
                                                    alignItems: 'flex-start',
                                                    marginBottom: 4,
                                                }}
                                            >
                                                <div
                                                    style={{
                                                        width: 12,
                                                        height: 12,
                                                        backgroundColor: color,
                                                        borderRadius: 2,
                                                        flexShrink: 0,
                                                        marginTop: 4,
                                                        marginRight: 8
                                                    }}
                                                />
                                                <Text style={{
                                                    margin: 0,
                                                    flex: 1,
                                                    lineHeight: '20px',
                                                    wordBreak: 'break-word',
                                                    whiteSpace: 'pre-wrap'
                                                }}>
                                                    {country} ： {Array.from(authorSet).join(', ')}
                                                </Text>
                                            </div>
                                        );
                                    })}
                                </div>
                            </div>



                        </Card>
                    </Col>

                    <Col xs={24} lg={12}>
                        <Card
                            title={
                                <span style={{ color: '#b82e28' }}>
                                    <GlobalOutlined style={{ marginRight: 8 }} />
                                    出版信息
                                </span>
                            }
                            className="info-card"
                        >
                            <div className="info-item">
                                <Text strong className="info-label">来源期刊：</Text>
                                <Text>{renderHtmlContent(paperInfo.journal_title_source || "暂无期刊信息")}</Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">出版商：</Text>
                                <Text>{getUnifiedPublisherName(paperInfo.publisher)}</Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">ISSN：</Text>
                                <Text>{paperInfo.identifier_issn || paperInfo.issn || "暂无ISSN"}</Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">EISSN：</Text>
                                <Text>{paperInfo.identifier_eissn || paperInfo.eissn || "暂无EISSN"}</Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">发表时间：</Text>
                                <Text>
                                    {paperInfo.pubyear}年
                                    {paperInfo.pubmonth && ` ${paperInfo.pubmonth}月`}
                                    {paperInfo.vol && ` ${paperInfo.vol}卷`}
                                    {paperInfo.issue && ` ${paperInfo.issue}期`}
                                </Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">语言：</Text>
                                <Tag color="blue">{paperInfo.languages || "English"}</Tag>
                            </div>
                        </Card>
                    </Col>
                </Row>

                {/* 摘要单独一行 */}
                <Card
                    title={
                        <span style={{ color: '#b82e28' }}>
                            <BankOutlined style={{ marginRight: 8 }} />
                            摘要
                        </span>
                    }
                    className="abstract-card"
                    style={{ marginTop: 16 }}
                >
                    <Paragraph
                        ellipsis={{
                            rows: 5,
                            expandable: true,
                            symbol: '展开全文'
                        }}
                        className="abstract-content"
                    >
                        {paperInfo.abstract_text ?
                            renderHtmlContent(paperInfo.abstract_text) :
                            "暂无摘要信息"
                        }
                    </Paragraph>
                </Card>

                {/* 学科方向和影响力 */}
                <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
                    <Col xs={24} lg={12}>
                        <Card
                            title={
                                <span style={{ color: '#b82e28' }}>
                                    学科方向
                                </span>
                            }
                            className="info-card"
                        >
                            <div className="info-item">
                                <Text strong className="info-label">关键词：</Text>
                                <div style={{ marginTop: 8 }}>
                                    {keywordInfo ?
                                        keywordInfo.split(/[;,]/).map((keyword, index) => (
                                            <Tag key={index} style={{ marginBottom: 4, marginRight: 4 }}>
                                                {renderHtmlContent(keyword.trim())}
                                            </Tag>
                                        )) :
                                        <Text type="secondary">暂无关键词</Text>
                                    }
                                </div>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">学科分类：</Text>
                                <Text>
                                    {renderHtmlContent(paperInfo.subheadings || paperInfo.subject_categories || paperInfo.categories || "暂无学科分类")}
                                </Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">研究领域：</Text>
                                <Text>
                                    {renderHtmlContent(paperInfo.subject_extended || paperInfo.research_area || paperInfo.research_domains || "暂无研究领域信息")}
                                </Text>
                            </div>
                        </Card>
                    </Col>

                    <Col xs={24} lg={12}>
                        <Card
                            title={
                                <span style={{ color: '#b82e28' }}>
                                    影响力指标
                                </span>
                            }
                            className="info-card"
                        >
                            <div className="info-item">
                                <Text strong className="info-label">页码范围：</Text>
                                <Text>
                                    {paperInfo.page_begin && paperInfo.page_end
                                        ? `${paperInfo.page_begin}-${paperInfo.page_end}`
                                        : paperInfo.pages || paperInfo.page_range || "暂无页码信息"
                                    }
                                </Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">文献类型：</Text>
                                <Tag color="green">
                                    {paperInfo.article_type || "不明确"}
                                </Tag>
                            </div>
                        </Card>
                    </Col>
                </Row>

                {/* 链接信息 */}
                <Card
                    title={
                        <span style={{ color: '#b82e28' }}>
                            <LinkOutlined style={{ marginRight: 8 }} />
                            文献链接与标识
                        </span>
                    }
                    className="link-card"
                    style={{ marginTop: 16 }}
                >
                    <Row gutter={[16, 16]}>
                        <Col xs={24} sm={12}>
                            <div className="info-item">
                                <Text strong className="info-label">DOI：</Text>
                                <Space direction="vertical" style={{ width: '100%' }}>
                                    <Text
                                        copyable={{ text: paperInfo.identifier_doi }}
                                        style={{ wordBreak: 'break-all' }}
                                    >
                                        {paperInfo.identifier_doi || paperInfo.doi || "暂无DOI"}
                                    </Text>
                                </Space>
                            </div>
                        </Col>
                        <Col xs={24} sm={12}>
                            <div className="info-item">
                                <Text strong className="info-label">原文链接：</Text>
                                <Space direction="vertical" style={{ width: '100%' }}>
                                    {paperUrl ? (
                                        <>
                                            <Text
                                                copyable={{ text: paperUrl }}
                                                ellipsis={{ tooltip: paperUrl }}
                                                style={{ wordBreak: 'break-all' }}
                                            >
                                                {paperUrl}
                                            </Text>
                                            <Button
                                                type="primary"
                                                size="small"
                                                icon={<LinkOutlined />}
                                                onClick={() => window.open(paperUrl, '_blank')}
                                                style={{ backgroundColor: '#b82e28' }}
                                            >
                                                访问原文
                                            </Button>
                                        </>
                                    ) : (
                                        <Text type="secondary">暂无可用链接</Text>
                                    )}
                                </Space>
                            </div>
                        </Col>
                    </Row>

                    {/* 其他标识信息 */}
                    <Row gutter={[16, 16]} style={{ marginTop: 16, paddingTop: 16, borderTop: '1px solid #f0f0f0' }}>
                        <Col xs={24} sm={8}>
                            <div className="info-item">
                                <Text strong className="info-label">IDS：</Text>
                                <Text copyable={{ text: paperInfo.identifier_accession_no }}>
                                    {paperInfo.identifier_accession_no || "暂无"}
                                </Text>
                            </div>
                        </Col>
                        <Col xs={24} sm={8}>
                            <div className="info-item">
                                <Text strong className="info-label">PubMed ID：</Text>
                                <Text copyable={{ text: getPubMedId() }}>
                                    {getPubMedId()}
                                </Text>
                            </div>
                        </Col>
                        <Col xs={24} sm={8}>
                            <div className="info-item">
                                <Text strong className="info-label">ISBN/EISBN：</Text>
                                <Text>
                                    {getIsbnInfo()}
                                </Text>
                            </div>
                        </Col>
                    </Row>
                </Card>
            </div>
        </div>
    );
}

export default DetailCard;