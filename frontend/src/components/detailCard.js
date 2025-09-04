import React from "react";
import '../styles/detailCard.css';
import { Card, Row, Col, Typography, Button, Tag, Space } from "antd";
import { LinkOutlined, GlobalOutlined, BankOutlined, UserOutlined } from '@ant-design/icons';

const { Title, Text, Paragraph } = Typography;

function DetailCard({ paperInfo }) {
    console.log(paperInfo);

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

    // 处理作者信息 - 基于实际字段
    const getAuthorInfo = () => {
        // 假设author_fullname包含多个作者，用分号分隔
        const authors = paperInfo.author_fullname ? paperInfo.author_fullname.split(';') : [];

        return {
            // 所有作者
            allAuthors: paperInfo.author_fullname || "暂无作者信息",
            // 第一作者（通常是第一个）
            firstAuthor: authors.length > 0 ? authors[0].trim() : "暂无信息",
            // 通讯作者（通常是最后一个，或者有特殊标记）
            correspondingAuthor: authors.length > 1 ? authors[authors.length - 1].trim() : authors[0]?.trim() || "暂无信息",
            // 作者机构信息
            institution: paperInfo.author_address || paperInfo.author_affiliations || "暂无机构信息"
        };
    };

    const authorInfo = getAuthorInfo();
    const paperUrl = getPaperUrl();

    return (
        <div className="detail-container">
            <div className="detail-content">
                {/* 论文标题 */}
                <Card className="title-card" bodyStyle={{ padding: '24px' }}>
                    <Title level={2} className="paper-title">
                        {paperInfo.article_title || "标题信息不可用"}
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
                                <Text>{authorInfo.allAuthors}</Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">第一作者：</Text>
                                <Text>{authorInfo.firstAuthor}</Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">通讯作者：</Text>
                                <Text>{authorInfo.correspondingAuthor}</Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">作者机构：</Text>
                                <Text>{authorInfo.institution}</Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">国家/地区：</Text>
                                <Text>{paperInfo.country || paperInfo.author_country || "暂无国家信息"}</Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">组织类型：</Text>
                                <Text>{paperInfo.organization_type || paperInfo.institution_type || "暂无组织信息"}</Text>
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
                                <Text>{paperInfo.journal_title_source || paperInfo.publisher || "暂无期刊信息"}</Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">ISSN：</Text>
                                <Text>{paperInfo.identifier_issn || "暂无ISSN"}</Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">EISSN：</Text>
                                <Text>{paperInfo.identifier_eissn || "暂无EISSN"}</Text>
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
                                <Tag color="blue">{paperInfo.languages || paperInfo.language || "English"}</Tag>
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
                        {paperInfo.abstract_text || "暂无摘要信息"}
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
                                    {paperInfo.keyword ?
                                        paperInfo.keyword.split(/[;,]/).map((keyword, index) => (
                                            <Tag key={index} style={{ marginBottom: 4, marginRight: 4 }}>
                                                {keyword.trim()}
                                            </Tag>
                                        )) :
                                        <Text type="secondary">暂无关键词</Text>
                                    }
                                </div>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">学科分类：</Text>
                                <Text>
                                    {paperInfo.subheadings || "暂无学科分类"}
                                </Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">研究领域：</Text>
                                <Text>
                                    {paperInfo.subject_extended || paperInfo.research_area || "暂无研究领域信息"}
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
                                <Text strong className="info-label">被引次数：</Text>
                                <Text className="metric-value">{paperInfo.citation_count || 0}</Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">下载次数：</Text>
                                <Text className="metric-value">{paperInfo.download_count || 0}</Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">页码范围：</Text>
                                <Text>
                                    {paperInfo.page_start && paperInfo.page_end
                                        ? `${paperInfo.page_start}-${paperInfo.page_end}`
                                        : paperInfo.pages || "暂无页码信息"
                                    }
                                </Text>
                            </div>
                            <div className="info-item">
                                <Text strong className="info-label">文献类型：</Text>
                                <Tag color="green">
                                    {paperInfo.document_type || paperInfo.publication_type || "研究论文"}
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
                                        {paperInfo.identifier_doi || "暂无DOI"}
                                    </Text>
                                    {paperInfo.identifier_doi && (
                                        <Button
                                            type="primary"
                                            size="small"
                                            icon={<LinkOutlined />}
                                            onClick={() => window.open(getDoiUrl(paperInfo.identifier_doi), '_blank')}
                                            style={{ backgroundColor: '#b82e28' }}
                                        >
                                            访问DOI链接
                                        </Button>
                                    )}
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
                                <Text strong className="info-label">WOS标识：</Text>
                                <Text copyable={{ text: paperInfo.wos_id }}>
                                    {paperInfo.wos_id || paperInfo.accession_number || "暂无"}
                                </Text>
                            </div>
                        </Col>
                        <Col xs={24} sm={8}>
                            <div className="info-item">
                                <Text strong className="info-label">PubMed ID：</Text>
                                <Text copyable={{ text: paperInfo.pmid }}>
                                    {paperInfo.pmid || paperInfo.pubmed_id || "暂无"}
                                </Text>
                            </div>
                        </Col>
                        <Col xs={24} sm={8}>
                            <div className="info-item">
                                <Text strong className="info-label">ISBN/其他：</Text>
                                <Text>
                                    {paperInfo.isbn || paperInfo.other_identifier || "暂无"}
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