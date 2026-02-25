/**
 * Unified Test Cases Component
 * Combines Test Case Generation, Storage, and Filtering in one page
 */
class UnifiedTestCasesComponent {
    constructor() {
        this.element = null;
        this.allTestCases = [];
        this.filteredTestCases = [];
        this.metrics = null;
        this.currentTab = 'generate'; // 'generate' or 'library'
        this.selectedTestCaseIds = new Set(); // Track selected test case IDs
    }

    async render(container) {
        this.element = container;
        
        const html = `
            <div class="page-header">
                <h1 class="page-title">📋 Test Cases</h1>
                <p class="page-description">Generate, view, and manage test cases</p>
            </div>

            <!-- Tab Navigation -->
            <div style="display: flex; gap: 0; margin-bottom: 20px; border-bottom: 2px solid #e5e7eb; padding-bottom: 0;">
                <button id="genTab" class="tab-btn active" style="padding: 12px 16px; background: none; border: none; font-weight: 600; color: #0052cc; cursor: pointer; border-bottom: 3px solid #0052cc; margin-bottom: -2px;">
                    ➕ Generate
                </button>
                <button id="libTab" class="tab-btn" style="padding: 12px 16px; background: none; border: none; font-weight: 600; color: #6b7280; cursor: pointer; border-bottom: 3px solid transparent; margin-bottom: -2px;">
                    📚 Library
                </button>
            </div>

            <!-- Generate Tab Content -->
            <div id="generateTab" class="tab-content">
                <!-- Input Section - Compact Layout -->
                <div class="card" style="margin-bottom: 20px;">
                    <div class="card-title">📝 Input Requirements</div>
                    
                    <div style="display: grid; grid-template-columns: 1fr 2fr; gap: 15px;">
                        <!-- File Upload (Compact) -->
                        <div>
                            <label class="form-label" style="font-size: 12px; font-weight: 600;">📄 Upload File</label>
                            <div class="file-upload-area" id="uploadArea" style="border: 2px dashed var(--border-color); padding: 12px; border-radius: 6px; text-align: center; cursor: pointer; transition: all 0.3s ease; background: var(--bg-light);">
                                <p style="font-size: 18px; margin: 0 0 4px 0;">+</p>
                                <p style="margin: 0; font-size: 11px; color: var(--text-secondary);">Click or drag</p>
                                <p style="margin: 3px 0 0 0; font-size: 10px; color: #999;">PDF, DOCX, TXT</p>
                                <input type="file" id="fileInput" accept=".pdf,.docx,.pptx,.txt" hidden>
                            </div>
                            <div id="uploadStatus" style="margin-top: 8px; font-size: 11px; color: var(--success);"></div>
                        </div>

                        <!-- Text Input & Config -->
                        <div>
                            <label class="form-label" style="font-size: 12px; font-weight: 600;">✏️ Requirements Text</label>
                            <textarea id="requirementsText" class="form-control" placeholder="Paste requirements or describe your test scenario..." rows="3" style="min-height: 100px; font-size: 12px; resize: vertical;"></textarea>
                            
                            <!-- Application and Module inputs removed per request -->
                            <button id="generateBtn" class="btn btn-primary" style="width: 100%; margin-top: 10px; font-size: 13px; padding: 8px;">🚀 Generate Test Cases</button>
                        </div>
                    </div>
                </div>

                <!-- Results -->
                <div id="resultsContainer"></div>
            </div>

            <!-- Library Tab Content -->
            <div id="libraryTab" class="tab-content" style="display: none;">
                <!-- Metrics -->
                <div class="grid grid-4" id="metricsSection" style="margin-bottom: 20px; gap: 15px;">
                    <!-- Will be populated -->
                </div>

                <!-- Filters & Code Generation -->
                <div class="card" style="margin-bottom: 20px;">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start; gap: 20px;">
                        <!-- Filters removed per request -->
                        <!-- Code Generation Section -->
                        <div style="flex: 0 0 auto; border-left: 1px solid var(--border-color); padding-left: 15px;">
                            <div style="font-weight: 600; margin-bottom: 8px; font-size: 13px;">💻 Code Generation</div>
                            <button id="openCodeGenModalBtn" class="btn btn-success" style="padding: 8px 14px; font-size: 12px; width: 100%; display: none; margin-bottom: 8px;">Generate Code</button>
                            <div id="selectedCountLabel" style="font-size: 11px; color: var(--text-secondary); text-align: center;">0 selected</div>
                        </div>
                    </div>
                </div>

                <!-- Test Cases Table -->
                <div id="libraryResultsContainer"></div>
            </div>
        `;
        
        this.element.innerHTML = html;
        this.setupEventListeners();
        await this.loadMetricsAndTestCases();
    }

    setupEventListeners() {
        // Tab switching
        document.getElementById('genTab').addEventListener('click', () => this.switchTab('generate'));
        document.getElementById('libTab').addEventListener('click', () => this.switchTab('library'));

        // File upload
        const uploadArea = document.getElementById('uploadArea');
        const fileInput = document.getElementById('fileInput');
        
        uploadArea.addEventListener('click', () => fileInput.click());
        uploadArea.addEventListener('dragover', (e) => {
            e.preventDefault();
            uploadArea.style.backgroundColor = '#f5f5f5';
        });
        uploadArea.addEventListener('dragleave', () => {
            uploadArea.style.backgroundColor = '#fff';
        });
        uploadArea.addEventListener('drop', (e) => {
            e.preventDefault();
            uploadArea.style.backgroundColor = '#fff';
            if (e.dataTransfer.files[0]) {
                this.handleFileUpload(e.dataTransfer.files[0]);
            }
        });
        fileInput.addEventListener('change', (e) => {
            if (e.target.files[0]) {
                this.handleFileUpload(e.target.files[0]);
            }
        });

        // Generate button
        document.getElementById('generateBtn').addEventListener('click', () => this.generateTestCases());

        // Filter removed: no apply button to wire
    }

    switchTab(tab) {
        this.currentTab = tab;
        
        const genTab = document.getElementById('genTab');
        const libTab = document.getElementById('libTab');
        const genContent = document.getElementById('generateTab');
        const libContent = document.getElementById('libraryTab');

        if (tab === 'generate') {
            genTab.style.color = '#0052cc';
            genTab.style.borderBottomColor = '#0052cc';
            libTab.style.color = '#6b7280';
            libTab.style.borderBottomColor = 'transparent';
            genContent.style.display = 'block';
            libContent.style.display = 'none';
        } else {
            genTab.style.color = '#6b7280';
            genTab.style.borderBottomColor = 'transparent';
            libTab.style.color = '#0052cc';
            libTab.style.borderBottomColor = '#0052cc';
            genContent.style.display = 'none';
            libContent.style.display = 'block';
        }
    }

    async loadMetricsAndTestCases() {
        try {
            const response = await fetch('/api/v1/testcases/metrics');
            const data = await response.json();
            this.metrics = data;
            console.log('Metrics loaded:', data);

            // Filters removed: metrics fetched for display only

            // Display metrics
            this.displayMetrics();

            // Load test cases
            const tcResponse = await fetch('/api/v1/testcases');
            const tcData = await tcResponse.json();
            console.log('Test cases loaded:', tcData);
            this.allTestCases = tcData.testCases || [];
            this.filteredTestCases = [...this.allTestCases];
            this.displayLibraryTestCases();
        } catch (error) {
            console.error('Error loading data:', error);
        }
    }

    // Filters removed: dropdown refresh not needed

    displayMetrics() {
        if (!this.metrics) return;
        const m = this.metrics;
        const metricsSection = document.getElementById('metricsSection');
        if (!metricsSection) return;

        metricsSection.innerHTML = `
            <div class="card" style="padding:12px; text-align:center;">
                <div style="font-weight:600; font-size:12px;">Total Test Cases</div>
                <div style="font-size:18px; margin-top:6px;">${m.totalTestCases || 0}</div>
            </div>
            <div class="card" style="padding:12px; text-align:center;">
                <div style="font-weight:600; font-size:12px;">Applications</div>
                <div style="font-size:18px; margin-top:6px;">${m.totalApplications || 0}</div>
            </div>
            <div class="card" style="padding:12px; text-align:center;">
                <div style="font-weight:600; font-size:12px;">Modules</div>
                <div style="font-size:18px; margin-top:6px;">${m.totalModules || 0}</div>
            </div>
            <div class="card" style="padding:12px; text-align:center;">
                <div style="font-weight:600; font-size:12px;">Tags</div>
                <div style="font-size:18px; margin-top:6px;">${m.totalTags || 0}</div>
            </div>
        `;
    }

    async generateTestCases() {
        const requirementsText = document.getElementById('requirementsText').value;
        const resultsDiv = document.getElementById('resultsContainer');

        if (!requirementsText.trim()) {
            resultsDiv.innerHTML = '<div class="alert alert-danger" style="margin-top: 20px;">Please enter requirements or upload a file.</div>';
            return;
        }

        const payload = { text: requirementsText };

        try {
            resultsDiv.innerHTML = '<div class="alert alert-info" style="margin-top: 20px;">Generating test cases...</div>';
            const response = await fetch('/api/v1/agents/testcases', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            const data = await response.json();
            
            if (response.ok && data.testCases && data.testCases.length > 0) {
                this.currentTestCases = data.testCases;
                
                let html = '<div class="card mt-20">';
                html += '<div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">';
                html += `<div class="card-title" style="margin: 0;">✓ Generated ${data.testCases.length} Test Cases</div>`;
                html += '<div style="display: flex; gap: 8px;">';
                html += '<button id="exportCSV" class="btn btn-secondary" style="padding: 6px 12px; font-size: 12px;">📥 CSV</button>';
                html += '<button id="exportExcel" class="btn btn-secondary" style="padding: 6px 12px; font-size: 12px;">📥 Excel</button>';
                html += '</div>';
                html += '</div>';
                
                data.testCases.forEach((tc, idx) => {
                    const tcId = `TC${String(idx + 1).padStart(3, '0')}`;
                    const priorityColor = tc.priority === 'HIGH' ? '#d9534f' : tc.priority === 'MEDIUM' ? '#f0ad4e' : '#5cb85c';
                    
                    html += `
                        <div style="border-left: 3px solid ${priorityColor}; padding: 12px; margin-bottom: 10px; background: var(--bg-light); border-radius: 4px;">
                            <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                                <strong style="font-size: 13px;">${tcId}: ${this.escapeHtml(tc.title || 'Test Case')}</strong>
                                <span style="background: ${priorityColor}; color: white; padding: 2px 8px; border-radius: 3px; font-size: 11px; font-weight: 600;">${tc.priority || 'MEDIUM'}</span>
                            </div>
                            <p style="margin: 5px 0; font-size: 12px; color: var(--text-secondary);">${this.escapeHtml(tc.description || '')}</p>
                            ${tc.steps && tc.steps.length > 0 ? `<p style="margin: 5px 0; font-size: 11px;"><strong>Steps:</strong> ${tc.steps.length}</p>` : ''}
                        </div>
                    `;
                });
                html += '</div>';
                resultsDiv.innerHTML = html;
                
                // Filters removed: no dropdown refresh needed
                
                document.getElementById('exportCSV').addEventListener('click', () => this.exportTestCasesAsCSV());
                document.getElementById('exportExcel').addEventListener('click', () => this.exportTestCasesAsExcel());
            } else {
                resultsDiv.innerHTML = '<div class="alert alert-danger" style="margin-top: 20px;">No test cases generated. Try with more detailed requirements.</div>';
            }
        } catch (error) {
            resultsDiv.innerHTML = `<div class="alert alert-danger" style="margin-top: 20px;">Error: ${error.message}</div>`;
        }
    }

    applyFilters() {
        // Filters removed: reset to full list
        this.filteredTestCases = [...this.allTestCases];
        this.displayLibraryTestCases();
    }

    displayLibraryTestCases() {
        if (this.filteredTestCases.length === 0) {
            document.getElementById('libraryResultsContainer').innerHTML = 
                '<div class="alert alert-info">No test cases found.</div>';
            return;
        }

        let html = `<div class="card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                <strong style="font-size: 13px;">Test Cases (${this.filteredTestCases.length})</strong>
                <div style="display: flex; gap: 8px;">
                    <button id="exportAllBtn" class="btn btn-secondary" style="padding: 5px 10px; font-size: 11px;">📥 Export</button>
                </div>
            </div>
            <div style="overflow-x: auto; font-size: 12px;">
                <table style="width: 100%; border-collapse: collapse;">
                    <thead>
                        <tr style="background: var(--bg-light); border-bottom: 1px solid var(--border-color);">
                            <th style="padding: 8px; text-align: center; font-weight: 600; width: 30px;">
                                <input type="checkbox" id="selectAllCheckbox" style="cursor: pointer;">
                            </th>
                            <th style="padding: 8px; text-align: left; font-weight: 600;">ID</th>
                            <th style="padding: 8px; text-align: left; font-weight: 600;">Title</th>
                            <th style="padding: 8px; text-align: left; font-weight: 600;">App</th>
                            <th style="padding: 8px; text-align: left; font-weight: 600;">Module</th>
                            <th style="padding: 8px; text-align: center; font-weight: 600;">Priority</th>
                            <th style="padding: 8px; text-align: center; font-weight: 600;">Steps</th>
                            <th style="padding: 8px; text-align: center; font-weight: 600;">Actions</th>
                        </tr>
                    </thead>
                    <tbody>`;

        this.filteredTestCases.forEach((tc, idx) => {
            const tcNumber = String(idx + 1).padStart(3, '0');
            const priorityColor = tc.priority === 'HIGH' ? '#d9534f' : tc.priority === 'MEDIUM' ? '#f0ad4e' : '#5cb85c';
            const isSelected = this.selectedTestCaseIds.has(tc.id);

            html += `
                <tr style="border-bottom: 1px solid var(--border-color); background: ${isSelected ? 'var(--bg-light)' : 'transparent'};">
                    <td style="padding: 8px; text-align: center;">
                        <input type="checkbox" class="tc-select" data-tc-id="${tc.id}" ${isSelected ? 'checked' : ''} style="cursor: pointer;">
                    </td>
                    <td style="padding: 8px;"><strong style="color: var(--primary-color);">TC${tcNumber}</strong></td>
                    <td style="padding: 8px;">${this.escapeHtml(tc.title || 'Untitled')}</td>
                    <td style="padding: 8px;">${this.escapeHtml(tc.appName || '-')}</td>
                    <td style="padding: 8px;">${this.escapeHtml(tc.module || '-')}</td>
                    <td style="padding: 8px; text-align: center;">
                        <span style="background: ${priorityColor}; color: white; padding: 2px 6px; border-radius: 3px; font-weight: 600; font-size: 11px;">${tc.priority || 'MEDIUM'}</span>
                    </td>
                    <td style="padding: 8px; text-align: center;">${(tc.steps || []).length}</td>
                    <td style="padding: 8px; text-align: center;">
                        <button class="view-tc-btn" data-tc-id="${tc.id}" style="background: #0052cc; color: white; border: none; padding: 4px 8px; border-radius: 3px; cursor: pointer; font-size: 11px;">View</button>
                    </td>
                </tr>
            `;
        });

        html += `
                    </tbody>
                </table>
            </div>
        </div>`;

        document.getElementById('libraryResultsContainer').innerHTML = html;

        // Setup checkbox event listeners
        this.setupCheckboxListeners();

        // Setup View test case button listeners
        const viewButtons = document.querySelectorAll('.view-tc-btn');
        viewButtons.forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const tcId = btn.getAttribute('data-tc-id');

                // Match using string coercion to avoid type mismatches
                let testCase = this.filteredTestCases.find(tc => String(tc.id) === String(tcId));

                // Fallback: if not found locally, try fetching from server
                if (!testCase) {
                    console.warn('Test case not found locally, fetching from server:', tcId);
                    try {
                        const resp = await fetch(`/api/v1/testcases/${encodeURIComponent(tcId)}`);
                        if (resp.ok) {
                            testCase = await resp.json();
                        } else {
                            console.error('Server returned', resp.status, 'for', tcId);
                            alert('Error: Test case not found on server.');
                            return;
                        }
                    } catch (err) {
                        console.error('Error fetching test case:', err);
                        alert('Error fetching test case details: ' + err.message);
                        return;
                    }
                }

                try {
                    this.showTestCaseModal(testCase);
                } catch (err) {
                    console.error('Error showing test case modal:', err);
                    alert('Failed to open test case view: ' + err.message);
                }
            });
        });

        const exportBtn = document.getElementById('exportAllBtn');
        if (exportBtn) {
            exportBtn.addEventListener('click', () => this.exportFilteredTestCases());
        }
    }

    setupCheckboxListeners() {
        const selectAllCheckbox = document.getElementById('selectAllCheckbox');
        const tcCheckboxes = document.querySelectorAll('.tc-select');

        // Select all checkbox
        if (selectAllCheckbox) {
            selectAllCheckbox.addEventListener('change', (e) => {
                tcCheckboxes.forEach(checkbox => {
                    checkbox.checked = e.target.checked;
                    const tcId = checkbox.getAttribute('data-tc-id');
                    if (e.target.checked) {
                        this.selectedTestCaseIds.add(tcId);
                    } else {
                        this.selectedTestCaseIds.delete(tcId);
                    }
                });
                this.updateGenerateCodeButtonVisibility();
                this.displayLibraryTestCases();
            });
        }

        // Individual checkboxes
        tcCheckboxes.forEach(checkbox => {
            checkbox.addEventListener('change', (e) => {
                const tcId = checkbox.getAttribute('data-tc-id');
                if (e.target.checked) {
                    this.selectedTestCaseIds.add(tcId);
                } else {
                    this.selectedTestCaseIds.delete(tcId);
                }
                this.updateGenerateCodeButtonVisibility();
            });
        });

        // Generate Code button (new location in filter section)
        const openCodeGenBtn = document.getElementById('openCodeGenModalBtn');
        if (openCodeGenBtn) {
            openCodeGenBtn.addEventListener('click', () => this.openCodeGenerationModal());
        }
    }

    updateGenerateCodeButtonVisibility() {
        const openCodeGenBtn = document.getElementById('openCodeGenModalBtn');
        const selectedCountLabel = document.getElementById('selectedCountLabel');
        
        if (openCodeGenBtn) {
            const hasSelected = this.selectedTestCaseIds.size > 0;
            openCodeGenBtn.style.display = hasSelected ? 'block' : 'none';
        }
        
        if (selectedCountLabel) {
            selectedCountLabel.textContent = `${this.selectedTestCaseIds.size} selected`;
        }
    }

    openCodeGenerationModal() {
        if (this.selectedTestCaseIds.size === 0) {
            alert('Please select at least one test case');
            return;
        }

        const selectedTestCases = Array.from(this.selectedTestCaseIds)
            .map(tcId => this.filteredTestCases.find(tc => tc.id === tcId))
            .filter(tc => tc !== undefined);

        console.log('Selected test cases for code generation:', selectedTestCases);

        // Create modal HTML
        const modalHTML = `
            <div id="codeGenerationModal" style="position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.5); display: flex; align-items: center; justify-content: center; z-index: 1000;">
                <div style="background: white; border-radius: 8px; padding: 30px; max-width: 500px; width: 90%; box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);">
                    <h2 style="margin-top: 0; margin-bottom: 20px; font-size: 18px; font-weight: 700;">💻 Generate Automation Code</h2>
                    
                    <div style="margin-bottom: 15px;">
                        <label style="display: block; font-weight: 600; margin-bottom: 6px; font-size: 13px;">Framework *</label>
                        <select id="frameworkSelect" style="width: 100%; padding: 8px 12px; border: 1px solid var(--border-color); border-radius: 4px; font-size: 12px;">
                            <option value="">-- Select Framework --</option>
                            <option value="SELENIUM">Selenium WebDriver</option>
                            <option value="PLAYWRIGHT">Playwright</option>
                        </select>
                    </div>

                    <div style="margin-bottom: 15px;">
                        <label style="display: block; font-weight: 600; margin-bottom: 6px; font-size: 13px;">Language *</label>
                        <select id="languageSelect" style="width: 100%; padding: 8px 12px; border: 1px solid var(--border-color); border-radius: 4px; font-size: 12px;">
                            <option value="">-- Select Language --</option>
                        </select>
                    </div>

                    <div style="margin-bottom: 15px;">
                        <label style="display: flex; align-items: center; font-size: 12px; cursor: pointer;">
                            <input type="checkbox" id="usePageObjectModel" checked style="margin-right: 8px; cursor: pointer;">
                            Use Page Object Model
                        </label>
                    </div>

                    <div style="margin-bottom: 20px;">
                        <label style="display: flex; align-items: center; font-size: 12px; cursor: pointer;">
                            <input type="checkbox" id="includeSetupTeardown" checked style="margin-right: 8px; cursor: pointer;">
                            Include Setup/Teardown Methods
                        </label>
                    </div>

                    <div style="display: flex; gap: 10px;">
                        <button id="cancelCodeGenBtn" class="btn btn-secondary" style="flex: 1; padding: 8px 12px;">Cancel</button>
                        <button id="submitCodeGenBtn" class="btn btn-primary" style="flex: 1; padding: 8px 12px;">Generate Code</button>
                    </div>
                </div>
            </div>
        `;

        // Remove existing modal if any
        const existingModal = document.getElementById('codeGenerationModal');
        if (existingModal) {
            existingModal.remove();
        }

        // Add modal to DOM
        document.body.insertAdjacentHTML('beforeend', modalHTML);

        // Setup event listeners
        const frameworkSelect = document.getElementById('frameworkSelect');
        const languageSelect = document.getElementById('languageSelect');
        const cancelBtn = document.getElementById('cancelCodeGenBtn');
        const submitBtn = document.getElementById('submitCodeGenBtn');

        // Framework change listener
        frameworkSelect.addEventListener('change', (e) => {
            const framework = e.target.value;
            this.updateLanguageOptions(framework);
        });

        // Cancel button
        cancelBtn.addEventListener('click', () => {
            document.getElementById('codeGenerationModal').remove();
        });

        // Submit button
        submitBtn.addEventListener('click', () => {
            const framework = frameworkSelect.value;
            const language = languageSelect.value;

            if (!framework || !language) {
                alert('Please select both Framework and Language');
                return;
            }

            this.generateCodeFromTestCases(
                selectedTestCases,
                framework,
                language,
                document.getElementById('usePageObjectModel').checked,
                document.getElementById('includeSetupTeardown').checked
            );
        });

        // Close modal when clicking outside
        document.getElementById('codeGenerationModal').addEventListener('click', (e) => {
            if (e.target.id === 'codeGenerationModal') {
                e.target.remove();
            }
        });
    }

    updateLanguageOptions(framework) {
        const languageSelect = document.getElementById('languageSelect');
        languageSelect.innerHTML = '<option value="">-- Select Language --</option>';

        const languages = {
            'SELENIUM': ['JAVA', 'PYTHON'],
            'PLAYWRIGHT': ['JAVASCRIPT', 'TYPESCRIPT', 'PYTHON']
        };

        if (languages[framework]) {
            languages[framework].forEach(lang => {
                const option = document.createElement('option');
                option.value = lang;
                option.textContent = this.getLanguageLabel(lang);
                languageSelect.appendChild(option);
            });
        }
    }

    getLanguageLabel(lang) {
        const labels = {
            'JAVA': 'Java',
            'PYTHON': 'Python',
            'JAVASCRIPT': 'JavaScript',
            'TYPESCRIPT': 'TypeScript'
        };
        return labels[lang] || lang;
    }

    async generateCodeFromTestCases(testCases, framework, language, usePageObjectModel, includeSetupTeardown) {
        try {
            const modal = document.getElementById('codeGenerationModal');
            const submitBtn = document.getElementById('submitCodeGenBtn');
            
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.textContent = 'Generating...';
            }

            const payload = {
                testCaseIds: Array.from(this.selectedTestCaseIds),
                framework: framework,
                language: language,
                usePageObjectModel: usePageObjectModel,
                includeSetupTeardown: includeSetupTeardown
            };

            console.log('Sending code generation request:', payload);

            const response = await fetch('/api/v1/agents/code/from-testcases', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            const data = await response.json();

            console.log('Response status:', response.status);
            console.log('Response data:', data);

            if (response.ok) {
                if (data.artifact) {
                    console.log('Code generated successfully:', data);
                    
                    // Close modal and show code viewer
                    if (modal) {
                        modal.remove();
                    }

                    this.displayCodeViewer(data.artifact, framework, language, testCases);
                } else {
                    throw new Error('No artifact in response: ' + JSON.stringify(data));
                }
            } else {
                const errorMsg = data.error || data.message || 'Unknown error';
                console.error('Error response:', errorMsg);
                throw new Error('Server error: ' + errorMsg);
            }
        } catch (error) {
            console.error('Error generating code:', error);
            alert('Error generating code: ' + error.message);
            const submitBtn = document.getElementById('submitCodeGenBtn');
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Generate Code';
            }
        }
    }

    displayCodeViewer(artifact, framework, language, testCases) {
        // Create a full-page overlay for code viewer
        const overlay = document.createElement('div');
        overlay.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 2000;
            overflow-y: auto;
            padding: 20px;
        `;

        const viewerContainer = document.createElement('div');
        viewerContainer.style.cssText = `
            width: 90%;
            max-width: 1200px;
            max-height: 90vh;
            background: white;
            border-radius: 8px;
            position: relative;
        `;

        overlay.appendChild(viewerContainer);
        document.body.appendChild(overlay);

        // Render code viewer component
        const codeViewer = new CodeViewerComponent(artifact, framework, language, testCases);
        codeViewer.render(viewerContainer);

        // Close overlay when clicking outside
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                overlay.remove();
                this.displayLibraryTestCases();
            }
        });

        // Also close when close button is clicked
        const originalCloseViewer = codeViewer.closeViewer;
        codeViewer.closeViewer = function() {
            overlay.remove();
        };
    }

    exportTestCasesAsCSV() {
        if (!this.currentTestCases || this.currentTestCases.length === 0) {
            alert('No test cases to export');
            return;
        }

        const csvContent = ExportUtils.generateCSV(this.currentTestCases);
        const timestamp = ExportUtils.getTimestamp();
        const filename = `TestCases_${this.currentAppName}_${timestamp}.csv`;
        ExportUtils.downloadFile(csvContent, filename, 'text/csv');
    }

    exportTestCasesAsExcel() {
        if (!this.currentTestCases || this.currentTestCases.length === 0) {
            alert('No test cases to export');
            return;
        }

        const worksheet = ExportUtils.createWorksheet(this.currentTestCases, this.currentAppName);
        const blob = new Blob([worksheet], { type: 'application/vnd.ms-excel;charset=utf-8;' });
        const timestamp = ExportUtils.getTimestamp();
        const filename = `TestCases_${this.currentAppName}_${timestamp}.xlsx`;
        ExportUtils.downloadFile(blob, filename, 'application/vnd.ms-excel');
    }

    exportFilteredTestCases() {
        if (this.filteredTestCases.length === 0) {
            alert('No test cases to export');
            return;
        }

        if (typeof ExportUtils !== 'undefined') {
            const csvContent = ExportUtils.generateCSV(this.filteredTestCases);
            const timestamp = ExportUtils.getTimestamp();
            ExportUtils.downloadFile(csvContent, `TestCases_Library_${timestamp}.csv`, 'text/csv');
        }
    }

    escapeHtml(text) {
        if (!text) return '';
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return String(text).replace(/[&<>"']/g, m => map[m]);
    }

    showTestCaseModal(testCase) {
        // Create modal HTML
        const stepsHtml = (testCase.steps || []).map((step, idx) => `
            <div style="margin-bottom: 12px; padding: 10px; background: #f9f9f9; border-left: 3px solid #0052cc; border-radius: 3px;">
                <p style="margin: 0 0 6px 0; font-weight: 600; font-size: 12px;">Step ${idx + 1}</p>
                <p style="margin: 0 0 4px 0; font-size: 11px; color: #666;"><strong>Action:</strong> ${this.escapeHtml(step.action || '')}</p>
                <p style="margin: 0; font-size: 11px; color: #666;"><strong>Expected:</strong> ${this.escapeHtml(step.expectedResult || '')}</p>
            </div>
        `).join('');

        const modalHtml = `
            <div style="position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000;">
                <div style="background: white; border-radius: 8px; box-shadow: 0 10px 40px rgba(0,0,0,0.2); max-width: 700px; width: 90%; max-height: 80vh; overflow-y: auto;">
                    <!-- Header -->
                    <div style="padding: 20px; border-bottom: 1px solid #e5e7eb; display: flex; justify-content: space-between; align-items: center; background: #f9fafb; border-radius: 8px 8px 0 0;">
                        <div>
                            <h2 style="margin: 0; font-size: 18px; font-weight: 700; color: #1f2937;">Test Case Details</h2>
                            <p style="margin: 4px 0 0 0; font-size: 12px; color: #6b7280;">ID: <strong>${this.escapeHtml(testCase.id || 'N/A')}</strong></p>
                        </div>
                        <button id="closeModalBtn" style="background: none; border: none; font-size: 24px; cursor: pointer; color: #6b7280; padding: 0; width: 30px; height: 30px; display: flex; align-items: center; justify-content: center;">✕</button>
                    </div>

                    <!-- Content -->
                    <div style="padding: 20px;">
                        <!-- Title -->
                        <div style="margin-bottom: 16px;">
                            <p style="margin: 0 0 6px 0; font-weight: 600; font-size: 12px; color: #6b7280; text-transform: uppercase;">Title</p>
                            <p style="margin: 0; font-size: 14px; color: #1f2937; font-weight: 600;">${this.escapeHtml(testCase.title || 'Untitled')}</p>
                        </div>

                        <!-- Metadata Grid -->
                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px;">
                            <!-- Application -->
                            <div>
                                <p style="margin: 0 0 6px 0; font-weight: 600; font-size: 12px; color: #6b7280; text-transform: uppercase;">Application</p>
                                <p style="margin: 0; font-size: 13px; color: #1f2937;">${this.escapeHtml(testCase.appName || '-')}</p>
                            </div>

                            <!-- Module -->
                            <div>
                                <p style="margin: 0 0 6px 0; font-weight: 600; font-size: 12px; color: #6b7280; text-transform: uppercase;">Module</p>
                                <p style="margin: 0; font-size: 13px; color: #1f2937;">${this.escapeHtml(testCase.module || '-')}</p>
                            </div>

                            <!-- Priority -->
                            <div>
                                <p style="margin: 0 0 6px 0; font-weight: 600; font-size: 12px; color: #6b7280; text-transform: uppercase;">Priority</p>
                                <p style="margin: 0;">
                                    <span style="background: ${testCase.priority === 'HIGH' ? '#d9534f' : testCase.priority === 'MEDIUM' ? '#f0ad4e' : '#5cb85c'}; color: white; padding: 4px 8px; border-radius: 3px; font-weight: 600; font-size: 12px;">${testCase.priority || 'MEDIUM'}</span>
                                </p>
                            </div>

                            <!-- Status -->
                            <div>
                                <p style="margin: 0 0 6px 0; font-weight: 600; font-size: 12px; color: #6b7280; text-transform: uppercase;">Status</p>
                                <p style="margin: 0; font-size: 13px; color: #1f2937;">${this.escapeHtml(testCase.status || 'PENDING')}</p>
                            </div>
                        </div>

                        <!-- Description -->
                        ${testCase.description ? `
                            <div style="margin-bottom: 20px; padding: 12px; background: #f0f4f8; border-left: 3px solid #0052cc; border-radius: 3px;">
                                <p style="margin: 0 0 6px 0; font-weight: 600; font-size: 12px; color: #1f2937;">Description</p>
                                <p style="margin: 0; font-size: 12px; color: #4b5563; line-height: 1.5;">${this.escapeHtml(testCase.description)}</p>
                            </div>
                        ` : ''}

                        <!-- Test Steps -->
                        <div style="margin-bottom: 20px;">
                            <h3 style="margin: 0 0 12px 0; font-weight: 700; font-size: 13px; color: #1f2937;">Test Steps (${testCase.steps ? testCase.steps.length : 0})</h3>
                            ${stepsHtml || '<p style="color: #6b7280; font-size: 12px;">No test steps defined.</p>'}
                        </div>

                        <!-- Pre-conditions and Post-conditions -->
                        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                            ${testCase.preconditions ? `
                                <div style="padding: 12px; background: #f0f4f8; border-left: 3px solid #0052cc; border-radius: 3px;">
                                    <p style="margin: 0 0 6px 0; font-weight: 600; font-size: 12px; color: #1f2937;">Pre-conditions</p>
                                    <p style="margin: 0; font-size: 11px; color: #4b5563; line-height: 1.4;">${this.escapeHtml(testCase.preconditions)}</p>
                                </div>
                            ` : ''}
                            ${testCase.postconditions ? `
                                <div style="padding: 12px; background: #f0f4f8; border-left: 3px solid #0052cc; border-radius: 3px;">
                                    <p style="margin: 0 0 6px 0; font-weight: 600; font-size: 12px; color: #1f2937;">Post-conditions</p>
                                    <p style="margin: 0; font-size: 11px; color: #4b5563; line-height: 1.4;">${this.escapeHtml(testCase.postconditions)}</p>
                                </div>
                            ` : ''}
                        </div>
                    </div>

                    <!-- Footer -->
                    <div style="padding: 16px; border-top: 1px solid #e5e7eb; background: #f9fafb; border-radius: 0 0 8px 8px; display: flex; justify-content: flex-end; gap: 10px;">
                        <button id="closeBtn" style="padding: 8px 16px; background: #e5e7eb; color: #1f2937; border: none; border-radius: 4px; cursor: pointer; font-weight: 600; font-size: 12px;">Close</button>
                    </div>
                </div>
            </div>
        `;

        // Create modal container
        const modalContainer = document.createElement('div');
        modalContainer.innerHTML = modalHtml;
        document.body.appendChild(modalContainer);

        // Add event listeners
        const closeModalBtn = modalContainer.querySelector('#closeModalBtn');
        const closeBtn = modalContainer.querySelector('#closeBtn');
        const modalOverlay = modalContainer.querySelector('div[style*="position: fixed"]');

        const closeModal = () => {
            modalContainer.remove();
        };

        closeModalBtn.addEventListener('click', closeModal);
        closeBtn.addEventListener('click', closeModal);
        modalOverlay.addEventListener('click', (e) => {
            if (e.target === modalOverlay) {
                closeModal();
            }
        });
    }
}
