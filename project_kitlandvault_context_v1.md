# Project KitLandVault: System Context Document
**Version:** 1.1 (May 2026)
**Owner:** Software Developer & Financial Planner

## 1. Project Overview
A personal finance web application built to manage a complex **"Bucket System"** (Zero-Based Budgeting) with integrated partner **Co-pay & Settlement** features.

## 2. Technical Stack
- **Frontend:** Angular v21 (Standalone Components, PWA)
- **Styling:** Tailwind CSS v4 + SCSS
- **Charting:** Chart.js
- **Backend:** Java Spring Boot 3.4 (Maven)
- **Database:** PostgreSQL (Ensuring data precision over MongoDB)
- **Infrastructure:** Docker & Docker Compose
- **Architecture:** Agent-native IDE compatible with MCP support

## 3. Financial Logic & Account Roles
The system operates on a **Transit -> Destination** flow. Monthly net income (~80,057.37 THB) is cleared from the transit account immediately.

### Account Structure (6 Buckets):
1. **BBL (Transit):** Receives salary. Used for credit card payments. Must reach 0 THB after transfers.
   - *Role:* `TRANSIT`
2. **Kasikorn (Daily/Household):** Primary wallet for daily spending.
   - *Role:* `DAILY`
   - *Logic:* Daily Allowance = (Balance - 3000 for Groceries) / Days remaining in month.
   - *Budget:* ~18,000 THB/month.
3. **LHB You (Bills & Buffer):** Command center for fixed costs.
   - *Role:* `BILLS`
   - *Logic:* Maintains min. balance of 10,000 THB.
   - *Payments:* Mortgage, Utilities, Internet, Subscriptions, Parents' allowance.
4. **SCB (Car Loan):** Strictly for car installments (~14,283 THB).
   - *Role:* `CAR_LOAN`
5. **Kept by Krungsri (Sinking Fund):** High-interest reserve.
   - *Role:* `SINKING_FUND`
   - *Sub-buckets (Goals):* Emergency (Mother's surgery 30k), Annual fees (HOA, Insurance), Car maintenance.
6. **Dime (Investment):** Holding for Allianz SE stock and S&P 500 Index Funds.
   - *Role:* `INVESTMENT`

## 4. Advanced Features (Requirements)
- **Internal Borrowing (Inter-Account):** Track transfers between the user's OWN wallets (e.g., Kasikorn borrowing from Dime) with a repayment tracker. Managed via `InternalTransfer` entity.
- **Inter-Person Loans:** Track loans between family members. Managed via `Loan` entity. Supports Full Payment and Installment modes.
- **Co-pay Module (Split 50/50):** Flag transactions where the partner owes half.
- **Advanced Payment:** Track 100% payments made for the partner.
- **Settlement Dashboard:** Shows net "Receivable from Partner" and updates wallet balance upon reimbursement (not classified as Income).

## 5. Current Financial State (May 2026 Reset)
- **Salary (Net):** 80,057.37 THB
- **Debt Clearing Strategy:** Using ~43,826 THB from Kept to clear high-balance 0% Credit Cards (KTC, FirstChoice, T1, Shopee) to reset the cash flow.
- **Future Targets:**
  - Allianz Stock (3+1 Company matching).
  - Vacation Home investment fund.
  - Mortgage Refinancing/Retention in 3 years.

## 6. Development Roadmap
1. **Database Schema:** Finalize `transactions`, `wallets`, `settlements`, `wallet_goals`, and `internal_transfers` tables.
2. **Backend Services:** Implement Daily Allowance (with reserve deduction) and Co-pay calculation logic.
3. **Frontend UI:** Build responsive dashboard with Tailwind CSS v4 progress bars for investment goals.
4. **Phase 2 (Deferred):** Credit card tracking, Investment module (stock/fund tracking).
