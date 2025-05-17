/**
 * Utility functions for the Mind GRAF graph management
 */

// Element types constants
export const ELEMENT_TYPES = {
    NODE: "Node",
    CABLE: "Cable",
    CONTEXT: "Context",
    ATTITUDE_FRAME: "Attitude Frame"
  };
  
  /**
   * Check if an element type should be represented as an edge
   * @param {string} type - The element type
   * @returns {boolean} - True if the element should be an edge
   */
  export const isEdgeType = (type) => {
    return type === ELEMENT_TYPES.CABLE;
  };
  
  /**
   * Generate a unique ID with a prefix
   * @param {string} prefix - Prefix for the ID
   * @param {number} counter - Counter value
   * @returns {string} - Generated ID
   */
  export const generateId = (prefix, counter) => {
    return `${prefix}-${counter}`;
  };
  
  /**
   * Get default style properties for a specific element type
   * @param {string} type - Element type
   * @returns {Object} - Style properties
   */
  export const getElementStyleByType = (type) => {
    switch (type) {
      case ELEMENT_TYPES.NODE:
        return {
          shape: "ellipse",
          bgColor: "#3b82f6",
          borderColor: "#2563eb",
          width: 50,
          height: 50,
        };
      case ELEMENT_TYPES.CONTEXT:
        return {
          shape: "rectangle",
          bgColor: "#10b981", 
          borderColor: "#059669",
          width: 80,
          height: 60,
        };
      case ELEMENT_TYPES.ATTITUDE_FRAME:
        return {
          shape: "diamond",
          bgColor: "#f59e0b",
          borderColor: "#d97706",
          width: 70,
          height: 70,
        };
      default:
        return {
          shape: "ellipse",
          bgColor: "#6b7280",
          borderColor: "#4b5563",
          width: 50,
          height: 50,
        };
    }
  };
  
  /**
   * Format data for export
   * @param {Object} cy - Cytoscape instance
   * @returns {Object} - Formatted graph data
   */
  export const exportGraphData = (cy) => {
    if (!cy) return null;
    
    return {
      nodes: cy.nodes().map(node => ({
        id: node.id(),
        type: node.data('type'),
        label: node.data('label'),
        position: node.position(),
        data: node.data()
      })),
      edges: cy.edges().map(edge => ({
        id: edge.id(),
        source: edge.data('source'),
        target: edge.data('target'),
        label: edge.data('label'),
        data: edge.data()
      }))
    };
  };
  
  /**
   * Import graph data to Cytoscape
   * @param {Object} cy - Cytoscape instance
   * @param {Object} data - Graph data
   */
  export const importGraphData = (cy, data) => {
    if (!cy || !data) return;
    
    cy.elements().remove(); // Clear existing elements
    
    // Add nodes first
    if (data.nodes) {
      data.nodes.forEach(node => {
        cy.add({
          group: 'nodes',
          data: node.data || { id: node.id, label: node.label, type: node.type },
          position: node.position
        });
      });
    }
    
    // Then add edges
    if (data.edges) {
      data.edges.forEach(edge => {
        cy.add({
          group: 'edges',
          data: edge.data || { 
            id: edge.id, 
            source: edge.source, 
            target: edge.target, 
            label: edge.label 
          }
        });
      });
    }
  };